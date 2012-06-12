function isFeasible = wheresched_X(x,p)

global operatorinstances;
global locationConstraints;
global memoryAvailable;
global memoryCost;
global sites;
global parentOpInst;
global opInstanceIndex;

disp('*** Starting Feasibility Function ***')

isFeasible = 1;

%Location-sensitive operators
for i=1:1:length(p)
   optInstId = operatorinstances{i};
   if has_key(locationConstraints, optInstId)
       siteId = get(locationConstraints, optInstId);
       %Check that operator instance is at correct site
       if p{i} ~= siteId
           isFeasible = 0;
       end;
   end;
end;

%Site memory constraints
memoryUsage = hashtable;

for i=1:1:length(sites)
   siteid = sites{i};
   memoryUsage = put(memoryUsage,siteid,0);
end

for i=1:1:length(p)
   siteid = p{i};
   operatorInstId= operatorinstances{i};
   memCost = get(memoryCost,operatorInstId);   
   memTotal = get(memoryUsage,siteid) + memCost;
   memoryUsage = put(memoryUsage,siteid,memTotal);
end

for i=1:1:length(sites)
   siteid = sites{i};
   if get(memoryAvailable,siteid) < get(memoryUsage,siteid)
       isFeasible = 0;
   end
end

%No circular paths, i.e., make sure that data does not go down the routing
%tree
for i=1:1:length(operatorinstances)
    sourceOpInst = operatorinstances{i};
    
    if has_key(parentOpInst, sourceOpInst)
        destOpInst = get(parentOpInst, sourceOpInst);

        sourceOpInstIndex = get(opInstanceIndex, sourceOpInst);
        destOpInstIndex = get(opInstanceIndex, destOpInst);

        sourceSite = p{sourceOpInstIndex};
        destSite = p{destOpInstIndex};
        
        if ~isAncestorSite(sourceSite, destSite)
            isFeasible = 0;
        end;
    end
end

if isFeasible==0
    disp('NOT FEASIBLE');
else
    disp('FEASIBLE')
end

end

%returns true if destSite is an ancestor site of sourceSite
function ancestor = isAncestorSite(sourceSite, destSite)
    global parentSite;

    if sourceSite==destSite
        ancestor = 1;
    elseif has_key(parentSite, sourceSite)
        %check next level up towards the root
        immediateParent = get(parentSite, sourceSite);
        ancestor = isAncestorSite(immediateParent, destSite);
    else
        %reached the root of routing tree and not found
        ancestor = 0;
    end
end
