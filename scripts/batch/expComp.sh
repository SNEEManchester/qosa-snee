######################################################################
### Experiment 2(a): Routing 			= Vanilla          ###
###                  Where-scheduling           = Vanilla          ###
###                  When-scheduling            = Vanilla          ###
######################################################################

#Scenario 1
exp-comp.py --do-avrora=false --net-num-nodes=100 --sneeql-network-file=$SNEEQLROOT/scripts/qos-exp/scenarios/ix-100-dense-net.xml --avrora-network-file=$SNEEQLROOT/scripts/qos-exp/scenarios/ix-100-dense-net.top --schema-file=$SNEEQLROOT/scripts/qos-exp/scenarios/100-node-min-schema.xml --acq-rates=60000 --qos-aware-routing=false --qos-aware-where-scheduling=false --qos-aware-when-scheduling=false --label=2a_scenario1 --compile-sneeql=true

#Scenario 2
exp-comp.py --do-avrora=false --net-num-nodes=100 --sneeql-network-file=$SNEEQLROOT/scripts/qos-exp/scenarios/ix-100-dense-net.xml --avrora-network-file=$SNEEQLROOT/scripts/qos-exp/scenarios/ix-100-dense-net.top --schema-file=$SNEEQLROOT/scripts/qos-exp/scenarios/100-node-maj-schema.xml --acq-rates=60000 --qos-aware-routing=false --qos-aware-where-scheduling=false --qos-aware-when-scheduling=false --label=2a_scenario2

#Scenario 3
exp-comp.py --do-avrora=false --net-num-nodes=100 --sneeql-network-file=$SNEEQLROOT/scripts/qos-exp/scenarios/ix-100-sparse-net.xml --avrora-network-file=$SNEEQLROOT/scripts/qos-exp/scenarios/ix-100-sparse-net.top --schema-file=$SNEEQLROOT/scripts/qos-exp/scenarios/100-node-min-schema.xml --acq-rates=60000 --qos-aware-routing=false --qos-aware-where-scheduling=false --qos-aware-when-scheduling=false --label=2a_scenario3

#Scenario 4
exp-comp.py --do-avrora=false --net-num-nodes=100 --sneeql-network-file=$SNEEQLROOT/scripts/qos-exp/scenarios/ix-100-sparse-net.xml --avrora-network-file=$SNEEQLROOT/scripts/qos-exp/scenarios/ix-100-sparse-net.top --schema-file=$SNEEQLROOT/scripts/qos-exp/scenarios/100-node-maj-schema.xml --acq-rates=60000 --qos-aware-routing=false --qos-aware-where-scheduling=false --qos-aware-when-scheduling=false --label=2a_scenario4

#Scenario 5
exp-comp.py --do-avrora=false --net-num-nodes=34 --sneeql-network-file=$SNEEQLROOT/scripts/qos-exp/scenarios/multihop.xml --avrora-network-file=$SNEEQLROOT/scripts/qos-exp/scenarios/multihop.top --schema-file=$SNEEQLROOT/scripts/qos-exp/scenarios/multihop-schema.xml --acq-rates=60000 --qos-aware-routing=false --qos-aware-where-scheduling=false --qos-aware-when-scheduling=false --label=2a_scenario5

######################################################################
### Experiment 2(b): Routing 			= Qos-aware        ###
###                  Where-scheduling           = Vanilla          ###
###                  When-scheduling            = Vanilla          ###
######################################################################

#Scenario 1
exp-comp.py --do-avrora=false --net-num-nodes=100 --sneeql-network-file=$SNEEQLROOT/scripts/qos-exp/scenarios/ix-100-dense-net.xml --avrora-network-file=$SNEEQLROOT/scripts/qos-exp/scenarios/ix-100-dense-net.top --schema-file=$SNEEQLROOT/scripts/qos-exp/scenarios/100-node-min-schema.xml --acq-rates=60000 --qos-aware-routing=true --qos-aware-where-scheduling=false --qos-aware-when-scheduling=false --routing-trees-to-generate=20 --routing-trees-to-keep=3 --label=2b_scenario1

#Scenario 2
exp-comp.py --do-avrora=false --net-num-nodes=100 --sneeql-network-file=$SNEEQLROOT/scripts/qos-exp/scenarios/ix-100-dense-net.xml --avrora-network-file=$SNEEQLROOT/scripts/qos-exp/scenarios/ix-100-dense-net.top --schema-file=$SNEEQLROOT/scripts/qos-exp/scenarios/100-node-maj-schema.xml --acq-rates=60000 --qos-aware-routing=true --qos-aware-where-scheduling=false --qos-aware-when-scheduling=false --routing-trees-to-generate=20 --routing-trees-to-keep=3 --label=2b_scenario2

#Scenario 3
exp-comp.py --do-avrora=false --net-num-nodes=100 --sneeql-network-file=$SNEEQLROOT/scripts/qos-exp/scenarios/ix-100-sparse-net.xml --avrora-network-file=$SNEEQLROOT/scripts/qos-exp/scenarios/ix-100-sparse-net.top --schema-file=$SNEEQLROOT/scripts/qos-exp/scenarios/100-node-min-schema.xml --acq-rates=60000 --qos-aware-routing=true --qos-aware-where-scheduling=false --qos-aware-when-scheduling=false --routing-trees-to-generate=20 --routing-trees-to-keep=3 --label=2b_scenario3

#Scenario 4
exp-comp.py --do-avrora=false --net-num-nodes=100 --sneeql-network-file=$SNEEQLROOT/scripts/qos-exp/scenarios/ix-100-sparse-net.xml --avrora-network-file=$SNEEQLROOT/scripts/qos-exp/scenarios/ix-100-sparse-net.top --schema-file=$SNEEQLROOT/scripts/qos-exp/scenarios/100-node-maj-schema.xml --acq-rates=60000 --qos-aware-routing=true --qos-aware-where-scheduling=false --qos-aware-when-scheduling=false --routing-trees-to-generate=20 --routing-trees-to-keep=3 --label=2b_scenario4

#Scenario 5
exp-comp.py --do-avrora=false --net-num-nodes=34 --sneeql-network-file=$SNEEQLROOT/scripts/qos-exp/scenarios/multihop.xml --avrora-network-file=$SNEEQLROOT/scripts/qos-exp/scenarios/multihop.top --schema-file=$SNEEQLROOT/scripts/qos-exp/scenarios/multihop-schema.xml --acq-rates=60000 --qos-aware-routing=true --qos-aware-where-scheduling=false --qos-aware-when-scheduling=false --routing-trees-to-generate=20 --routing-trees-to-keep=3 --label=2b_scenario5

######################################################################
### Experiment 2(c): Routing 			= Vanilla          ###
###                  Where-scheduling           = Qos-aware        ###
###                  When-scheduling            = Vanilla          ###
######################################################################
 
#Scenario 1
exp-comp.py --do-avrora=false --net-num-nodes=100 --sneeql-network-file=$SNEEQLROOT/scripts/qos-exp/scenarios/ix-100-dense-net.xml --avrora-network-file=$SNEEQLROOT/scripts/qos-exp/scenarios/ix-100-dense-net.top --schema-file=$SNEEQLROOT/scripts/qos-exp/scenarios/100-node-min-schema.xml --acq-rates=60000 --qos-aware-routing=false --qos-aware-where-scheduling=true --qos-aware-when-scheduling=false --label=2c_scenario1

#Scenario 2
exp-comp.py --do-avrora=false --net-num-nodes=100 --sneeql-network-file=$SNEEQLROOT/scripts/qos-exp/scenarios/ix-100-dense-net.xml --avrora-network-file=$SNEEQLROOT/scripts/qos-exp/scenarios/ix-100-dense-net.top --schema-file=$SNEEQLROOT/scripts/qos-exp/scenarios/100-node-maj-schema.xml --acq-rates=60000 --qos-aware-routing=false --qos-aware-where-scheduling=true --qos-aware-when-scheduling=false --label=2c_scenario2

#Scenario 3
exp-comp.py --do-avrora=false --net-num-nodes=100 --sneeql-network-file=$SNEEQLROOT/scripts/qos-exp/scenarios/ix-100-sparse-net.xml --avrora-network-file=$SNEEQLROOT/scripts/qos-exp/scenarios/ix-100-sparse-net.top --schema-file=$SNEEQLROOT/scripts/qos-exp/scenarios/100-node-min-schema.xml --acq-rates=60000 --qos-aware-routing=false --qos-aware-where-scheduling=true --qos-aware-when-scheduling=false --label=2c_scenario3

#Scenario 4
exp-comp.py --do-avrora=false --net-num-nodes=100 --sneeql-network-file=$SNEEQLROOT/scripts/qos-exp/scenarios/ix-100-sparse-net.xml --avrora-network-file=$SNEEQLROOT/scripts/qos-exp/scenarios/ix-100-sparse-net.top --schema-file=$SNEEQLROOT/scripts/qos-exp/scenarios/100-node-maj-schema.xml --acq-rates=60000 --qos-aware-routing=false --qos-aware-where-scheduling=true --qos-aware-when-scheduling=false --label=2c_scenario4

#Scenario 5
exp-comp.py --do-avrora=false --net-num-nodes=34 --sneeql-network-file=$SNEEQLROOT/scripts/qos-exp/scenarios/multihop.xml --avrora-network-file=$SNEEQLROOT/scripts/qos-exp/scenarios/multihop.top --schema-file=$SNEEQLROOT/scripts/qos-exp/scenarios/multihop-schema.xml --acq-rates=60000 --qos-aware-routing=false --qos-aware-where-scheduling=true --qos-aware-when-scheduling=false --label=2c_scenario5


######################################################################
### Experiment 2(d): Routing 			= Vanilla          ###
###                  Where-scheduling           = Vanilla          ###
###                  When-scheduling            = Qos-aware        ###
######################################################################

#Scenario 1
exp-comp.py --do-avrora=false --net-num-nodes=100 --sneeql-network-file=$SNEEQLROOT/scripts/qos-exp/scenarios/ix-100-dense-net.xml --avrora-network-file=$SNEEQLROOT/scripts/qos-exp/scenarios/ix-100-dense-net.top --schema-file=$SNEEQLROOT/scripts/qos-exp/scenarios/100-node-min-schema.xml --acq-rates=60000 --qos-aware-routing=false --qos-aware-where-scheduling=false --qos-aware-when-scheduling=true --label=2d_scenario1

#Scenario 2
exp-comp.py --do-avrora=false --net-num-nodes=100 --sneeql-network-file=$SNEEQLROOT/scripts/qos-exp/scenarios/ix-100-dense-net.xml --avrora-network-file=$SNEEQLROOT/scripts/qos-exp/scenarios/ix-100-dense-net.top --schema-file=$SNEEQLROOT/scripts/qos-exp/scenarios/100-node-maj-schema.xml --acq-rates=60000 --qos-aware-routing=false --qos-aware-where-scheduling=false --qos-aware-when-scheduling=true --label=2d_scenario2

#Scenario 3
exp-comp.py --do-avrora=false --net-num-nodes=100 --sneeql-network-file=$SNEEQLROOT/scripts/qos-exp/scenarios/ix-100-sparse-net.xml --avrora-network-file=$SNEEQLROOT/scripts/qos-exp/scenarios/ix-100-sparse-net.top --schema-file=$SNEEQLROOT/scripts/qos-exp/scenarios/100-node-min-schema.xml --acq-rates=60000 --qos-aware-routing=false --qos-aware-where-scheduling=false --qos-aware-when-scheduling=true --label=2d_scenario3

#Scenario 4
exp-comp.py --do-avrora=false --net-num-nodes=100 --sneeql-network-file=$SNEEQLROOT/scripts/qos-exp/scenarios/ix-100-sparse-net.xml --avrora-network-file=$SNEEQLROOT/scripts/qos-exp/scenarios/ix-100-sparse-net.top --schema-file=$SNEEQLROOT/scripts/qos-exp/scenarios/100-node-maj-schema.xml --acq-rates=60000 --qos-aware-routing=false --qos-aware-where-scheduling=false --qos-aware-when-scheduling=true --label=2d_scenario4

#Scenario 5
exp-comp.py --do-avrora=false --net-num-nodes=34 --sneeql-network-file=$SNEEQLROOT/scripts/qos-exp/scenarios/multihop.xml --avrora-network-file=$SNEEQLROOT/scripts/qos-exp/scenarios/multihop.top --schema-file=$SNEEQLROOT/scripts/qos-exp/scenarios/multihop-schema.xml --acq-rates=60000 --qos-aware-routing=false --qos-aware-where-scheduling=false --qos-aware-when-scheduling=true --label=2d_scenario5

######################################################################
### Experiment 2(e): Routing 			= Qos-aware        ###
###                  Where-scheduling           = QoS-aware        ###
###                  When-scheduling            = Qos-aware        ###
######################################################################

#Scenario 1
exp-comp.py --do-avrora=false --net-num-nodes=100 --sneeql-network-file=$SNEEQLROOT/scripts/qos-exp/scenarios/ix-100-dense-net.xml --avrora-network-file=$SNEEQLROOT/scripts/qos-exp/scenarios/ix-100-dense-net.top --schema-file=$SNEEQLROOT/scripts/qos-exp/scenarios/100-node-min-schema.xml --acq-rates=60000 --qos-aware-routing=true --qos-aware-where-scheduling=true --qos-aware-when-scheduling=true --routing-trees-to-generate=20 --routing-trees-to-keep=3 --label=2e_scenario1

#Scenario 2
exp-comp.py --do-avrora=false --net-num-nodes=100 --sneeql-network-file=$SNEEQLROOT/scripts/qos-exp/scenarios/ix-100-dense-net.xml --avrora-network-file=$SNEEQLROOT/scripts/qos-exp/scenarios/ix-100-dense-net.top --schema-file=$SNEEQLROOT/scripts/qos-exp/scenarios/100-node-maj-schema.xml --acq-rates=60000 --qos-aware-routing=true --qos-aware-where-scheduling=true --qos-aware-when-scheduling=true --routing-trees-to-generate=20 --routing-trees-to-keep=3 --label=2e_scenario2

#Scenario 3
exp-comp.py --do-avrora=false --net-num-nodes=100 --sneeql-network-file=$SNEEQLROOT/scripts/qos-exp/scenarios/ix-100-sparse-net.xml --avrora-network-file=$SNEEQLROOT/scripts/qos-exp/scenarios/ix-100-sparse-net.top --schema-file=$SNEEQLROOT/scripts/qos-exp/scenarios/100-node-min-schema.xml --acq-rates=60000 --qos-aware-routing=true --qos-aware-where-scheduling=true --qos-aware-when-scheduling=true --routing-trees-to-generate=20 --routing-trees-to-keep=3 --label=2e_scenario3

#Scenario 4
exp-comp.py --do-avrora=false --net-num-nodes=100 --sneeql-network-file=$SNEEQLROOT/scripts/qos-exp/scenarios/ix-100-sparse-net.xml --avrora-network-file=$SNEEQLROOT/scripts/qos-exp/scenarios/ix-100-sparse-net.top --schema-file=$SNEEQLROOT/scripts/qos-exp/scenarios/100-node-maj-schema.xml --acq-rates=60000 --qos-aware-routing=true --qos-aware-where-scheduling=true --qos-aware-when-scheduling=true --routing-trees-to-generate=20 --routing-trees-to-keep=3 --label=2e_scenario4

#Scenario 5
exp-comp.py --do-avrora=false --net-num-nodes=34 --sneeql-network-file=$SNEEQLROOT/scripts/qos-exp/scenarios/multihop.xml --avrora-network-file=$SNEEQLROOT/scripts/qos-exp/scenarios/multihop.top --schema-file=$SNEEQLROOT/scripts/qos-exp/scenarios/multihop-schema.xml --acq-rates=60000 --qos-aware-routing=true --qos-aware-where-scheduling=true --qos-aware-when-scheduling=true --routing-trees-to-generate=20 --routing-trees-to-keep=3 --label=2e_scenario5
