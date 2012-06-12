
#A short version of this experimentm for testing only
#10 nodes, majority sources
scripts/batch/exp-rt-score.py --short --do-tossim=false --do-avrora=false --do-avrora-candidates=false --num-avrora-runs=1 --generate-random-network=False --generate-random-schemas=False --compile-sneeql=true --net-num-nodes=10 --sneeql-network-file=$SNEEQLROOT/scripts/qos-exp/scenarios/ix-10-net.xml --avrora-network-file=$SNEEQLROOT/scripts/qos-exp/scenarios/ix-10-net.top --schema-file=$SNEEQLROOT/scripts/qos-exp/scenarios/10-node-maj-schema.xml --routing-trees-to-generate=500 --routing-trees-to-keep=10

#A short version of this experimentm for testing only
#10 nodes, minority sources
#exp-rt-score.py --short --do-tossim=false --do-avrora=true --do-avrora-candidates=true --num-avrora-runs=1 --generate-random-network=False --generate-random-schemas=False --compile-sneeql=true --net-num-nodes=10 --sneeql-network-file=$SNEEQLROOT/scripts/qos-exp/scenarios/ix-10-net.xml --avrora-network-file=$SNEEQLROOT/scripts/qos-exp/scenarios/ix-10-net.top --schema-file=$SNEEQLROOT/scripts/qos-exp/scenarios/10-node-min-schema.xml --routing-trees-to-generate=10 --routing-trees-to-keep=10

#A not-too-short version of this experiment for testing; the aim to to get sufficient variation in the routing tree population, and (hopefully) a signifcant difference in the QoS-results
#exp-rt-score.py --short --do-tossim=false --do-avrora=true --do-avrora-candidates=true --num-avrora-runs=1 --generate-random-network=False --generate-random-schemas=False --compile-sneeql=true --net-num-nodes=30 --sneeql-network-file=$SNEEQLROOT/scripts/qos-exp/scenarios/30-dense-net.xml --avrora-network-file=$SNEEQLROOT/scripts/qos-exp/scenarios/30-dense-net.top --schema-file=$SNEEQLROOT/scripts/qos-exp/scenarios/30-node-maj-schema.xml --acq-rates=8000 --routing-trees-to-generate=10 --routing-trees-to-keep=10

#Scenario 1
#exp-rt-score.py --short --do-tossim=false --do-avrora=true --do-avrora-candidates=true --num-avrora-runs=1 --generate-random-network=False --generate-random-schemas=False --compile-sneeql=true --net-num-nodes=100 --sneeql-network-file=$SNEEQLROOT/scripts/qos-exp/scenarios/ix-100-dense-net.xml --avrora-network-file=$SNEEQLROOT/scripts/qos-exp/scenarios/ix-100-dense-net.top --schema-file=$SNEEQLROOT/scripts/qos-exp/scenarios/100-node-min-schema.xml --acq-rates=40000 --routing-trees-to-generate=10 --routing-trees-to-keep=10

#Scenario 2
#exp-rt-score.py --short --do-tossim=false --do-avrora=true --do-avrora-candidates=true --num-avrora-runs=1 --generate-random-network=False --generate-random-schemas=False --compile-sneeql=true --net-num-nodes=100 --sneeql-network-file=$SNEEQLROOT/scripts/qos-exp/scenarios/ix-100-dense-net.xml --avrora-network-file=$SNEEQLROOT/scripts/qos-exp/scenarios/ix-100-dense-net.top --schema-file=$SNEEQLROOT/scripts/qos-exp/scenarios/100-node-maj-schema.xml --acq-rates=40000 --routing-trees-to-generate=10 --routing-trees-to-keep=10

#Scenario 3
#exp-rt-score.py --short --do-tossim=false --do-avrora=true --do-avrora-candidates=true --num-avrora-runs=1 --generate-random-network=False --generate-random-schemas=False --compile-sneeql=true --net-num-nodes=100 --sneeql-network-file=$SNEEQLROOT/scripts/qos-exp/scenarios/ix-100-sparse-net.xml --avrora-network-file=$SNEEQLROOT/scripts/qos-exp/scenarios/ix-100-sparse-net.top --schema-file=$SNEEQLROOT/scripts/qos-exp/scenarios/100-node-min-schema.xml --acq-rates=40000 --routing-trees-to-generate=10 --routing-trees-to-keep=10

#Scenario 4 
#exp-rt-score.py --short --do-tossim=false --do-avrora=true --do-avrora-candidates=true --num-avrora-runs=1 --generate-random-network=False --generate-random-schemas=False --compile-sneeql=true --net-num-nodes=100 --sneeql-network-file=$SNEEQLROOT/scripts/qos-exp/scenarios/ix-100-sparse-net.xml --avrora-network-file=$SNEEQLROOT/scripts/qos-exp/scenarios/ix-100-sparse-net.top --schema-file=$SNEEQLROOT/scripts/qos-exp/scenarios/100-node-maj-schema.xml --acq-rates=40000 --routing-trees-to-generate=10 --routing-trees-to-keep=10
