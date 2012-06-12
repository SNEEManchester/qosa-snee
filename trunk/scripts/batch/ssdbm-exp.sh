
#Experiment 1 on a 10 node network
#Plots figures 14 (Energy consumption vs Alpha) and 15 (Network longevity vs Alpha)
#ssdbm-exp.py --compile-sneeql=true --net-num-nodes=10 --sneeql-network-file=$SNEEQLROOT/input/Networks/ssdbm-10-node-network.nss --net-num-nodes=10 --schema-file=$SNEEQLROOT/input/Pipes/ssdbm-10-node-schemas.xml --acq-rates=2000,3000,5000,7000,10000,15000,20000,30000,60000 --queries=Q0,Q2,Q3temp --max-buffering-factors=1 --x-val-type=acq 
#2000,3000,5000,7000,10000,15000,20000,30000, 60000
#Q0,Q2,Q3temp


#ssdbm-exp.py --compile-sneeql=true --net-num-nodes=30 --sneeql-network-file=$SNEEQLROOT/input/Networks/ssdbm-30-node-network.nss --net-num-nodes=30 --schema-file=$SNEEQLROOT/input/Pipes/ssdbm-30-node-schema.xml --acq-rates=12000,15000,20000,30000,60000,100000,150000 --queries=Q0,Q2,Q3temp --max-buffering-factors=1 --x-val-type=acq 
#12000,15000,20000,30000,60000,100000,150000

#Experiment for Network longevity vs Delta
ssdbm-exp.py --compile-sneeql=true --net-num-nodes=30 --sneeql-network-file=$SNEEQLROOT/input/Networks/ssdbm-30-node-network.nss --net-num-nodes=30 --schema-file=$SNEEQLROOT/input/Pipes/ssdbm-30-node-schema.xml --acq-rates=20000 --delivery-times=20000,40000 --queries=Q3temp --x-val-type=del
#20000,40000,60000,100000,120000,240000 #no need to do 120000, 240000 as bf==5
#Q0,Q2,Q3temp #for Q3temp only do 20000 and 40000, as max bf=2