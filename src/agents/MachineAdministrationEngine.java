
package agents;

import jade.core.AID;
import jade.util.leap.Iterator;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;
import ontology.order.terms.Order;
import ontology.tool.terms.SettingSheet;
import ontology.tool.terms.Tool;
import ontology.tool.terms.ToolInstance;
import ontology.tool.terms.ToolRequirementEntry;
import ontology.tool.terms.ToolRequirementList;
import ontology.tool.terms.UsetimeListEntry;

/**
 * Utiltity class handling MachineAgent's internal state and projetion calculations
 *
 * @author Shahin Mahmody
 */
public class MachineAdministrationEngine {
    private static final DecimalFormat decimalFormat = new DecimalFormat("0.00");
    
    private final ArrayList<MachiningOrder> orderQueue;
    //stores tool instances for machining
    private final HashMap<String, ToolInstance> magazineStorage;
    
    private final String[] magazineSlots = {"ms_01","ms_02","ms_03","ms_04","ms_05",
                                                "ms_06","ms_07","ms_08","ms_09","ms_10"};
    
    // the locking order is the currently examined order, which forbids others 
    // from changing anything during evaluation
    private MachiningOrder currentLockingOrder = null;
    
    public MachineAdministrationEngine(){
        orderQueue = new ArrayList<MachiningOrder>();
        magazineStorage = new HashMap<String, ToolInstance>();
        for(String slotName: magazineSlots){
            magazineStorage.put(slotName,null);
        }
    }
    
    public String getLockingOrderId(){
        if(currentLockingOrder == null){
            return null;
        }else{
            return currentLockingOrder.id;
        }
    }
    
    public boolean hasLockingOrder(){
        return currentLockingOrder != null;
    }
    
    public void setLockingOrder(Order order, SettingSheet settingSheet, AID orderingAgent, String conversationId){
        if(currentLockingOrder == null){
            MachiningOrder newOrder = new MachiningOrder(conversationId);
            newOrder.order = order;
            newOrder.settingSheet = settingSheet;
            newOrder.orderAgent = orderingAgent;
            currentLockingOrder = newOrder;
        }
    }
    
    public void setLockingOrderSettingSheet(SettingSheet settingSheet){
        if(currentLockingOrder != null && currentLockingOrder.settingSheet == null){
            currentLockingOrder.settingSheet = settingSheet;
        }
    }
    
    public void releaseLockingOrder() {
        currentLockingOrder = null;
    }
    
    public void insertLockingOrderIntoQueue(int position, float projectedCost, float projectedTime){
        currentLockingOrder.projectedCost = projectedCost;
        currentLockingOrder.projectedTime = projectedTime;
        currentLockingOrder.projectedSavings = 0;
        orderQueue.add(position,currentLockingOrder);
        
        releaseLockingOrder();
    }
    
    public SettingSheet getMachineOrderSettingSheet(String id){
        if(currentLockingOrder != null && currentLockingOrder.id.equals(id)){
            return currentLockingOrder.settingSheet;
        }else{
            for(MachiningOrder mOrder: orderQueue){
                if(mOrder.id.equals(id)){
                    return mOrder.settingSheet;
                }
            }
        }
        return null;
    }

    public Order getMachineOrderOrder(String id){
        if(currentLockingOrder != null && currentLockingOrder.id.equals(id)){
            return currentLockingOrder.order;
        }else{
            for(MachiningOrder mOrder: orderQueue){
                if(mOrder.id.equals(id)){
                    return mOrder.order;
                }
            }
        }
        return null;
    }
    
    public AID getMachineOrderClient(String id){
        if(currentLockingOrder != null && currentLockingOrder.id.equals(id)){
            return currentLockingOrder.orderAgent;
        }else{
            for(MachiningOrder mOrder: orderQueue){
                if(mOrder.id.equals(id)){
                    return mOrder.orderAgent;
                }
            }
        }
        return null;
    }
    
    public HashMap<String,ToolRequirementEntry> getMachineOrderGrossToollife(int position){
        if(orderQueue.size() > position){
            return orderQueue.get(position).grossToolUsageMap;
        }else{
            return null;
        }
    }
    
    public float getMachineOrderCost(String id){
        if(currentLockingOrder != null && currentLockingOrder.id.equals(id)){
            return currentLockingOrder.projectedCost;
        }else{
            for(MachiningOrder mOrder: orderQueue){
                if(mOrder.id.equals(id)){
                    return mOrder.projectedCost;
                }
            }
        }
        return 0;
    }
    
    public String getMachineOrderIdByPosition(int position){
        return orderQueue.get(position).id;
    }
    
    public ToolRequirementList getMachineOrderProjectedToolRequirement(String id){
        if(currentLockingOrder != null && currentLockingOrder.id.equals(id)){
            return currentLockingOrder.projectedUsedTools;
        }else{
            for(MachiningOrder mOrder: orderQueue){
                if(mOrder.id.equals(id)){
                    return mOrder.projectedUsedTools;
                }
            }
        }
        return null;
    }
    
    public void setMachineOrderProjectedToolRequirement(String id, ToolRequirementList trl){
        if(currentLockingOrder != null && currentLockingOrder.id.equals(id)){
            currentLockingOrder.projectedUsedTools = trl;
        }else{
            for(MachiningOrder mOrder: orderQueue){
                if(mOrder.id.equals(id)){
                    mOrder.projectedUsedTools = trl;
                }
            }
        }
    }
    
    public void updateProjections(String orderId, float projectedCosts, float projectedTime){
        for(MachiningOrder mOrder: orderQueue){
            if(mOrder.id.equals(orderId)){
                mOrder.projectedCost = projectedCosts;
                mOrder.projectedTime = projectedTime;
            }
        }
    }
    
    /**
     * executes the order in the first position: Reduces the lifetimes of used 
     * tools, destroys tools with too much spent lifetime
     * 
     * @return null on error case, list of destroyed tools otherwise
     */
    public ArrayList<ToolInstance> executeOrder(){
        ArrayList<ToolInstance> destroyedTools = new ArrayList<ToolInstance>();
        //Tool Annihilation
        for(ToolRequirementEntry grossToolUsage: orderQueue.get(0).grossToolUsageMap.values()){
            float totalUseTime = grossToolUsage.getUsetime();
            //locate tools of matching type within the magazine slots
            ArrayList<Map.Entry<String,ToolInstance>> orderedSlotList = new ArrayList<Map.Entry<String,ToolInstance>>();
            for(Map.Entry<String,ToolInstance> magazineEntry : magazineStorage.entrySet()){
                if(magazineEntry.getValue() != null && 
                        magazineEntry.getValue().getTool().getToolId().equals(grossToolUsage.getTool().getToolId())){
                    insertMagazineSlotIntoOrderedList(magazineEntry, orderedSlotList);
                }
            }
            //reduce lifetimes
            for(Map.Entry<String,ToolInstance> magazineEntry : orderedSlotList){
                if(totalUseTime > 0 && magazineEntry.getValue() != null &&
                        magazineEntry.getValue().getTool().getToolId().equals(grossToolUsage.getTool().getToolId())){
                    ToolInstance magazineTool = magazineStorage.get(magazineEntry.getKey());
                    float remainingLifeTime = magazineTool.getTool().getLifetime()-magazineTool.getUsedTime()-
                                    magazineTool.getTool().getCriticaltime();
                    float subtrahend = Math.min(remainingLifeTime,totalUseTime);
                    totalUseTime -= subtrahend;
                    //take note of now unusable tools
                    magazineTool.setUsedTime(magazineTool.getUsedTime() + subtrahend);
                    if(subtrahend == remainingLifeTime){
                        destroyedTools.add(magazineTool);
                        magazineEntry.setValue(null);
                    }
                }
            }
            //error case where machining time could not be fulfilled by loaded tools
            if(totalUseTime > 0){
                return null;
            }
        }
        
        return destroyedTools;
    }
    
    /**
     * removes first entry in the order queue
     */
    public void adjustQueue(){
        orderQueue.remove(0);
    }
    
    public AID getOrderingAgent(int position){
        return orderQueue.get(position).orderAgent;
    }
    
    /**
     * Returns the tools that will be taken out of the magazine 
     * after the order at <code>position</code> is finished.
     * 
     * This is done by ordering loaded tools 
     * based on the next time they are going to be used. Afterwards 
     * the tools with the highest calculated value get removed until
     * there is enough space to load the entire next order. Ties
     * are resolved by alphanumeric ordering of the tool id, since this
     * needs to be deterministic (alphanumerically lower tools stay longer).
     * Priorities are how many orders away the next use of the tool is
     * @param position the position in the queue to be analysed
     * @param projectedOrderQueue the hypothetical queue during analysis
     * @param projectedToolConfiguration projection of the tool magazine 
     * @return the tools that will be taken out of the magazine 
     * after the order at <code>position</code> is finished.
     */
    private LinkedList<ToolPriority> getToolCleanup(int position,
            ArrayList<MachiningOrder> projectedOrderQueue, HashMap<String, ToolInstance> projectedToolConfiguration){
        LinkedList<ToolPriority> toolPriorities = new LinkedList<ToolPriority>();

        //build tool priorities list
        for(Map.Entry<String, ToolInstance> entry: projectedToolConfiguration.entrySet()){
            toolPriorities.add(new ToolPriority(entry.getValue(),entry.getKey(), projectedOrderQueue.size()+1));
        }
        //assign priorities
        for(int i = position ; i < projectedOrderQueue.size(); i++){
            Iterator neededToolsIterator = projectedOrderQueue.get(i).settingSheet.getToolRequirementsList().getToolRequirements().iterator();
            while(neededToolsIterator.hasNext()){
                String neededTool = ((ToolRequirementEntry)neededToolsIterator.next()).getTool().getToolId();
                for(ToolPriority tp: toolPriorities){
                    if(tp.tool != null && tp.tool.getTool().getToolId().equals(neededTool)){
                        tp.priority = i - position + 1;
                    }
                }
            }
        }
        
        toolPriorities.sort(new Comparator<ToolPriority>(){
            @Override
            public int compare(ToolPriority o1, ToolPriority o2) {
                if(o1.priority != o2.priority){
                    return o2.priority - o1.priority;
                }else{
                    if(o1.tool == null){
                        return -1;
                    }else if(o2.tool == null){
                        return 1;
                    }
                    if(o2.tool.getTool().getToolId().equals(o1.tool.getTool().getToolId())){
                        if(o1.tool.getUsedTime() < o2.tool.getUsedTime()){
                            return -1;
                        }else if(o1.tool.getUsedTime() > o2.tool.getUsedTime()){
                            return 1;
                        }else{
                            return 0;
                        }
                    }else{
                        return o2.tool.getTool().getToolId().compareTo(o1.tool.getTool().getToolId());
                    }
                }
            }
        });
        return toolPriorities;
    }
    
    /**
     * Exchange tools on the magazine for new ones
     * @param toolsFromStorage 
     * @return list of tools that were removed, null on error case
     */
    public jade.util.leap.List mountTools(jade.util.leap.List toolsFromStorage){
        jade.util.leap.List toolsForTakeout = new jade.util.leap.ArrayList();
        LinkedList<ToolPriority> toolTakeOutPriorities = getToolCleanup(0, orderQueue, magazineStorage);
        ArrayList<String> slotsToBeEmptied = new ArrayList<String>();
        if(toolsFromStorage == null){
            toolsFromStorage = new jade.util.leap.ArrayList();
        }
        HashMap<String,ArrayList<ToolInstance>> storedToolMap = getOrderedStoredToolsMapFromList(toolsFromStorage);
        
        for(ToolRequirementEntry grossToolUsage: orderQueue.get(0).grossToolUsageMap.values()){
            float storedLifetime = 0;
            //how much is already in the magazine
            for(ToolInstance magazineTool: magazineStorage.values()){
                if(magazineTool != null && magazineTool.getTool().getToolId().equals(grossToolUsage.getTool().getToolId())){
                    storedLifetime += magazineTool.getTool().getLifetime() - 
                        magazineTool.getTool().getCriticaltime() - magazineTool.getUsedTime();
                }
            }
           //get more from storage
            java.util.List<ToolInstance> storedToolList = storedToolMap.get(grossToolUsage.getTool().getToolId());
            if(storedToolList != null){
                ListIterator<ToolInstance> storedInstanceIterator = 
                        storedToolList.listIterator();
                while(storedInstanceIterator.hasNext() && storedLifetime < grossToolUsage.getUsetime()){
                    ToolInstance storedInstance = storedInstanceIterator.next();
                    ToolPriority nextForTakeout = toolTakeOutPriorities.poll();
                    //but first check if the capacity limit has been reached!
                    if(nextForTakeout == null || nextForTakeout.priority == 1){
                        //we have reached the limit, error case
                        return null;
                    }
                    //don't add to the lists if the slot was empty anyway!
                    if(nextForTakeout.tool != null){
                        //add to tools for takeout
                        toolsForTakeout.add(nextForTakeout.tool);
                        slotsToBeEmptied.add(nextForTakeout.slotId);
                    }
                    storedLifetime += storedInstance.getTool().getLifetime()- storedInstance.getTool().getCriticaltime()- storedInstance.getUsedTime();
                }
            }
        }
        
        //remove from slots
        for(String slotId: slotsToBeEmptied){
            magazineStorage.replace(slotId, null);
        }
        //insert into slots
        Iterator newToolsIterator = toolsFromStorage.iterator();
        for(Map.Entry<String,ToolInstance> magazineEntry: magazineStorage.entrySet()){
            if(magazineEntry.getValue() == null){
                if(newToolsIterator.hasNext()){
                    magazineEntry.setValue((ToolInstance)newToolsIterator.next());
                }
            }
        }
        setFirstOrderTooled(true);
        
        return toolsForTakeout;
    }
    
    public Collection<Tool> getAllRequiredToolTypes(SettingSheet newOrder){
        HashMap<String, Tool> toolMap = new HashMap<String, Tool>();
        Iterator newOrderToolsIterator = newOrder.getToolRequirementsList().getToolRequirements().iterator();
        
        while(newOrderToolsIterator.hasNext()){
            ToolRequirementEntry tre = (ToolRequirementEntry) newOrderToolsIterator.next();
            toolMap.putIfAbsent(tre.getTool().getToolId(), tre.getTool());
        }
        for(MachiningOrder mOrder: orderQueue){
            newOrderToolsIterator = mOrder.settingSheet.getToolRequirementsList().getToolRequirements().iterator();
            while(newOrderToolsIterator.hasNext()){
                ToolRequirementEntry tre = (ToolRequirementEntry) newOrderToolsIterator.next();
                toolMap.putIfAbsent(tre.getTool().getToolId(), tre.getTool());
            }
        }
        
        return toolMap.values();
    }
    /**
     * Calculates net tool requirements of all orders if the locking order
     * were hypothetically inserted into the queue
     * @param position the locking order's hypothetical position
     * @param toolsInStorage tools in storage at the time of execution
     * @return list of tool requirements per order, null if any order is not fulfillable
     */
    public jade.util.leap.List getNetToolLists(int position, jade.util.leap.List toolsInStorage){
        jade.util.leap.List netToolLists = new jade.util.leap.ArrayList();
        HashMap<String, ArrayList<ToolInstance>> storedToolMap = getOrderedStoredToolsMapFromList(toolsInStorage);
        HashMap<String, ToolInstance> projectedMagazine = new HashMap <String, ToolInstance>();
        ArrayList<MachiningOrder> projectedQueue = new ArrayList<MachiningOrder>();
        
        projectedQueue.addAll(orderQueue);
        projectedQueue.add(position, currentLockingOrder);
        for(int orderCounter = 0; orderCounter < projectedQueue.size(); orderCounter++){
            MachiningOrder mOrder = projectedQueue.get(orderCounter);
            ToolRequirementList netToolList = new ToolRequirementList(new jade.util.leap.ArrayList(),ToolRequirementList.TYPE_USETIME);
            
            float previousOrderMachiningTime = 0;
            if(orderCounter > 0){
                MachiningOrder previousOrder = projectedQueue.get(orderCounter-1);
                previousOrderMachiningTime = (previousOrder.settingSheet.getThroughputTime()* previousOrder.order.getQuantity()) 
                        + previousOrder.settingSheet.getSetupTime();
            }
            netToolLists.add(new UsetimeListEntry(mOrder.id,netToolList,
                    BigDecimal.valueOf(
                            (mOrder.settingSheet.getThroughputTime()* mOrder.order.getQuantity()) + mOrder.settingSheet.getSetupTime()).setScale(2,RoundingMode.HALF_UP).floatValue(),
                    previousOrderMachiningTime));
            //calculate grossRequirement if not already present
            if(mOrder.grossToolUsageMap == null){
                calculateToolUsetimesInMachiningOrder(mOrder);
            }
            
            if(orderCounter == 0){
                //clone current magazine
                for(Map.Entry<String,ToolInstance> magazineEntry: magazineStorage.entrySet()){
                    ToolInstance instance = magazineEntry.getValue();
                    //make a copy of existing tools so they don't get overwritten!
                    if(instance != null){
                        instance = new ToolInstance(instance.getTool(), 
                                instance.getInstanceId(), instance.getUsedTime(),0, 0);
                    }
                    projectedMagazine.put(magazineEntry.getKey(), instance);
                }
            }
            
            LinkedList<ToolPriority> toolTakeOutPriorities = getToolCleanup(orderCounter,projectedQueue, projectedMagazine);

            if(orderCounter > 0 || !mOrder.tooled){
                //exchange stored tools with magazine slots
                for(ToolRequirementEntry grossToolUsage: mOrder.grossToolUsageMap.values()){
                    ToolRequirementEntry netRequirementEntry = new ToolRequirementEntry(grossToolUsage.getTool(),
                        grossToolUsage.getUsetime(),0,0);
                    netToolList.getToolRequirements().add(netRequirementEntry);
                    ArrayList<Map.Entry<String,ToolInstance>> orderedSlotList = new ArrayList<Map.Entry<String,ToolInstance>>();
                    float storedLifetime = 0;
                    //how much is already in the magazine
                    for(Map.Entry<String,ToolInstance> magazineTool: projectedMagazine.entrySet()){
                        if(magazineTool.getValue() != null && magazineTool.getValue().getTool().getToolId().equals(grossToolUsage.getTool().getToolId())){
                            storedLifetime += magazineTool.getValue().getTool().getLifetime() - 
                                magazineTool.getValue().getTool().getCriticaltime() - magazineTool.getValue().getUsedTime();
                            insertMagazineSlotIntoOrderedList(magazineTool, orderedSlotList);
                        }
                    }
                    //take already stored tools into consideration
                    java.util.List<ToolInstance> storedToolList = storedToolMap.get(grossToolUsage.getTool().getToolId());
                    if(storedToolList != null){
                        ListIterator<ToolInstance> storedInstanceIterator = 
                                storedToolList.listIterator();
                        while(storedInstanceIterator.hasNext() && storedLifetime < grossToolUsage.getUsetime()){
                            ToolInstance storedInstance = storedInstanceIterator.next();
                            ToolPriority nextForTakeout = toolTakeOutPriorities.poll();
                            //but first check if the capacity limit has been reached!
                                //-> due to the simplicity of the usecase we always use stored tools ahead of new ones
                                //   which can lead to situations where an order could be fulfilled with new tools, but
                                //   too many old ones prevent execution!
                            if(nextForTakeout == null || nextForTakeout.priority == 1){
                                return null; //unfulfillable!
                            }
                            //add to tools-from-storage count
                            netRequirementEntry.setQuantity(netRequirementEntry.getQuantity()+1);
                            //replace a tool in magazine with the stored tool
                            storedInstanceIterator.remove();
                            projectedMagazine.put(nextForTakeout.slotId,storedInstance);
                            insertMagazineSlotIntoOrderedList(new AbstractMap.SimpleEntry<String,ToolInstance>(nextForTakeout.slotId,storedInstance) , orderedSlotList);
                            if(nextForTakeout.tool != null){
                                storedToolMap.putIfAbsent(storedInstance.getTool().getToolId()
                                    , new ArrayList<ToolInstance>());
                                storedToolMap.get(storedInstance.getTool().getToolId()).add(nextForTakeout.tool);
                            }
                            storedLifetime += storedInstance.getTool().getLifetime()- storedInstance.getTool().getCriticaltime()- storedInstance.getUsedTime();
                        }
                    }
                    
                    //calculate netRequirement and mount new tools
                    netRequirementEntry.setUsetime(netRequirementEntry.getUsetime()- storedLifetime);
                    
                    BigDecimal templateLifetimeRounder = new BigDecimal(netRequirementEntry.getTool().getLifetime() /grossToolUsage.getTemplateToolLife());
                    float requiredToolMaxLifetime = (netRequirementEntry.getTool().getLifetime() - netRequirementEntry.getTool().getCriticaltime()) / 
                           templateLifetimeRounder.setScale(2, RoundingMode.HALF_UP).floatValue();
                    
                    for(float remainingRequiredLifetime = netRequirementEntry.getUsetime();
                            remainingRequiredLifetime > 0;
                            remainingRequiredLifetime -= requiredToolMaxLifetime){
                        ToolPriority nextForTakeout = toolTakeOutPriorities.poll();
                        //but first check if the capacity limit has been reached!
                        //this check is necessary because a) there may have been
                        //no previous check if there were no stored tools or b)
                        //stored tools + new tools exceeds magazine limit
                        if(nextForTakeout == null || nextForTakeout.priority == 1){
                            //we have reached the limit
                            return null;
                        }
                        //replace a tool in magazine with the new Tool
                        if(nextForTakeout.tool != null){
                            storedToolMap.putIfAbsent(nextForTakeout.tool.getTool().getToolId()
                                , new ArrayList<ToolInstance>());
                            storedToolMap.get(nextForTakeout.tool.getTool().getToolId()).add(nextForTakeout.tool);
                        }
                        ToolInstance newInstance = new ToolInstance(grossToolUsage.getTool(),"new",0,0,0);
                        projectedMagazine.put(nextForTakeout.slotId, newInstance);
                        insertMagazineSlotIntoOrderedList(new AbstractMap.SimpleEntry<String,ToolInstance>(nextForTakeout.slotId,newInstance) , orderedSlotList);
                            
                    }
                    
                    //Tool Annihilation
                    float totalUseTime = grossToolUsage.getUsetime();
                    for(Map.Entry<String,ToolInstance> magazineEntry : orderedSlotList){
                        if(totalUseTime > 0 && magazineEntry.getValue() != null &&
                                magazineEntry.getValue().getTool().getToolId().equals(grossToolUsage.getTool().getToolId())){
                            ToolInstance magazineTool = projectedMagazine.get(magazineEntry.getKey());
                            float remainingLifeTime = magazineTool.getTool().getLifetime()-magazineTool.getUsedTime()-
                                            magazineTool.getTool().getCriticaltime();
                            float subtrahend = Math.min(remainingLifeTime,totalUseTime);
                            totalUseTime -= subtrahend;
                            //discard spent tools
                            magazineTool.setUsedTime(magazineTool.getUsedTime() + subtrahend);
                            if(subtrahend == remainingLifeTime){
                                magazineEntry.setValue(null);
                            }
                        }
                    }
                }
            }else{
                for(ToolRequirementEntry grossToolUsage: mOrder.grossToolUsageMap.values()){
                    float totalUseTime = grossToolUsage.getUsetime();
                    ArrayList<Map.Entry<String,ToolInstance>> orderedSlotList = new ArrayList<Map.Entry<String,ToolInstance>>();
                    for(Map.Entry<String,ToolInstance> magazineEntry : projectedMagazine.entrySet()){
                        if(magazineEntry.getValue() != null && 
                                magazineEntry.getValue().getTool().getToolId().equals(grossToolUsage.getTool().getToolId())){
                            insertMagazineSlotIntoOrderedList(magazineEntry, orderedSlotList);
                        }
                    }
                    for(Map.Entry<String,ToolInstance> magazineEntry : orderedSlotList){
                        if(totalUseTime > 0 && magazineEntry.getValue() != null && 
                                magazineEntry.getValue().getTool().getToolId().equals(grossToolUsage.getTool().getToolId())){
                            ToolInstance magazineTool = projectedMagazine.get(magazineEntry.getKey());
                            float remainingLifeTime = magazineTool.getTool().getLifetime()-magazineTool.getUsedTime()-
                                            magazineTool.getTool().getCriticaltime();
                            float subtrahend = Math.min(remainingLifeTime,totalUseTime);
                            totalUseTime -= subtrahend;
                            //discard spent tools
                            magazineTool.setUsedTime(magazineTool.getUsedTime() + subtrahend);
                            if(subtrahend == remainingLifeTime){
                                magazineEntry.setValue(null);
                            }
                        }
                    }
                }
            }
        }
        return netToolLists;
    }
   
    /**
     * Turns a flat list of tools into an ordered map
     * @param tools
     * @return the tools mapped according to tool type, ordered internally by 
     * remaining lifetime, descending
     */
    public HashMap<String, ArrayList<ToolInstance>> getOrderedStoredToolsMapFromList(jade.util.leap.List tools){
        HashMap<String, ArrayList<ToolInstance>> orderedToolMap = new HashMap<String, ArrayList<ToolInstance>>();
        Iterator toolListIterator = tools.iterator();
        while(toolListIterator.hasNext()){
            ToolInstance storedInstance = (ToolInstance) toolListIterator.next();
            ArrayList<ToolInstance> toolInstanceList;
            if((toolInstanceList = orderedToolMap.putIfAbsent(storedInstance.getTool().getToolId(), new ArrayList<ToolInstance>())) == null){
                toolInstanceList = orderedToolMap.get(storedInstance.getTool().getToolId());
            }
            if(toolInstanceList.isEmpty()){
                 toolInstanceList.add(new ToolInstance(storedInstance.getTool(),"",storedInstance.getUsedTime(),0,0));
            }else{
                for(int i = 0; i < toolInstanceList.size(); i++){
                    if(toolInstanceList.get(i).getUsedTime()<= storedInstance.getUsedTime()){
                        //make new Tool Instances so the methods can manipulate the objects without influencing other calculations
                        toolInstanceList.add(i, new ToolInstance(storedInstance.getTool(),storedInstance.getInstanceId()
                                ,storedInstance.getUsedTime(),0,0));
                        break;
                    }
                    //append if the last one is still healthier
                    if(i+1 == toolInstanceList.size()){
                        toolInstanceList.add(new ToolInstance(storedInstance.getTool(),storedInstance.getInstanceId()
                                ,storedInstance.getUsedTime(),0,0));
                        break;
                    }
                }
            }
        }
        return orderedToolMap;
    }
    
    public int getQueueSize(){
        return orderQueue.size();
    }
    
    /**
     * Calculates a MachiningOrders gross tool requirements
     * @param mOrder 
     */
    private void calculateToolUsetimesInMachiningOrder(MachiningOrder mOrder){
        Iterator toolRequirementIterator = mOrder.settingSheet.getToolRequirementsList().getToolRequirements().iterator();
        mOrder.grossToolUsageMap = new HashMap<String,ToolRequirementEntry>();
        while(toolRequirementIterator.hasNext()){
            ToolRequirementEntry tre = ((ToolRequirementEntry)toolRequirementIterator.next());
            BigDecimal templateLifetimeRounder = new BigDecimal(tre.getTool().getLifetime()/ tre.getTemplateToolLife());
            float usetime = tre.getUsetime() * mOrder.order.getQuantity() *
                        templateLifetimeRounder.setScale(2, RoundingMode.HALF_UP).floatValue();
            mOrder.grossToolUsageMap.put(tre.getTool().getToolId(),new ToolRequirementEntry(tre.getTool(),usetime,0,tre.getTemplateToolLife()));
        }
    }
    
    public void insertOrder(int position, Order order, SettingSheet settingSheet, AID orderAgent, String conversationId){
        MachiningOrder mOrder = new MachiningOrder(conversationId);
        mOrder.order = order;
        mOrder.settingSheet = settingSheet;
        mOrder.orderAgent = orderAgent;
        if(position <= orderQueue.size()){
            orderQueue.add(position,mOrder);
        }else{
            orderQueue.add(mOrder);
        }
    }
    
    public void setFirstOrderTooled(boolean tooled){
        if(!orderQueue.isEmpty()){
            orderQueue.get(0).tooled = tooled;
        }
    }
    
    public boolean isFirstOrderTooled(){
        return orderQueue.get(0) == null || orderQueue.get(0).tooled;
    }
    
    public HashMap<String,ToolInstance> getMagazine(){
        return magazineStorage;
    }
    
    /**
     * Utility function putting a magazine-slot-tool-instance pair into its 
     * appropriate place in a list that is ordered by remaining lifetime, descending
     * @param slot
     * @param slotList 
     */
    private void insertMagazineSlotIntoOrderedList(Map.Entry<String,ToolInstance> slot, List<Map.Entry<String,ToolInstance>> slotList){
        if(slotList.isEmpty() || slot.getValue().getUsedTime() == 0){
            slotList.add(slot);
        }else{
            for(int i = 0; i< slotList.size(); i++){
                if(slot.getValue().getUsedTime()> slotList.get(i).getValue().getUsedTime()){
                    slotList.add(i,slot);
                    break;
                }else if(i+1 == slotList.size()){
                    slotList.add(slot);
                    break;
                }
            }
        }
    }
    
    private class MachiningOrder{
        public String id;
        public Order order = null;
        public SettingSheet settingSheet = null;
        public AID orderAgent = null;
        public float projectedCost = 0;
        public float projectedTime = 0;
        public float projectedSavings = 0;
        //required usetime for all workpieces for each tool in the settingsheet, modified by template use time factor
        public HashMap<String,ToolRequirementEntry> grossToolUsageMap = null;
        public boolean tooled = false;
        public ToolRequirementList projectedUsedTools;
        
        public MachiningOrder(String conversationId){
            id = conversationId;
        }
    }
    
    private class ToolPriority{
        public ToolInstance tool;
        public String slotId;
        public int priority;

        public ToolPriority(ToolInstance tool, String slotId, int priority) {
            this.tool = tool;
            this.slotId = slotId;
            this.priority = priority;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 67 * hash + Objects.hashCode(this.tool);
            hash = 67 * hash + Objects.hashCode(this.slotId);
            hash = 67 * hash + this.priority;
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ToolPriority other = (ToolPriority) obj;
            if (this.priority != other.priority) {
                return false;
            }
            if (!Objects.equals(this.slotId, other.slotId)) {
                return false;
            }
            if (!Objects.equals(this.tool, other.tool)) {
                return false;
            }
            return true;
        }
    }
    
    //~~~~~TESTING ONLY!~~~~~
    public void setMagazineTools(jade.util.leap.List tools){
        if(tools != null && !tools.isEmpty()){
            Iterator toolIterator = tools.iterator();
            for(Map.Entry<String,ToolInstance> entry : magazineStorage.entrySet()){
                if(toolIterator.hasNext()){
                    entry.setValue((ToolInstance) toolIterator.next());
                }else{
                    entry.setValue(null);
                }
            }
        }else{
            for(Map.Entry<String,ToolInstance> entry : magazineStorage.entrySet()){
                entry.setValue(null);
            }
        }
    }
    
    public void clearOrders(){
        orderQueue.clear(); 
    }
    
    public void calculateGrossRequirements(String id){
        for(MachiningOrder mOrder: orderQueue){
            if(mOrder.id.equals(id)){
                calculateToolUsetimesInMachiningOrder(mOrder);
                return;
            }
        }
    }
}
