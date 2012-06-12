#avrora variance testing -- first run (small network)
#we plotted total energy (not lifetime)
#avrora-variance.py --short --do-tossim=false --do-avrora=true --do-avrora-candidates=false --num-avrora-runs=100 --num-source-nodes=7 --output-root=/cygdrive/c/ixent/results --net-num-nodes=10 --net-x-dim=100 --net-y-dim=100 --generate-random-network=True

avrora-variance.py --short --do-tossim=false --do-avrora=true --do-avrora-candidates=false --num-avrora-runs=100 --num-source-nodes=4 --output-root=/cygdrive/c/ixent/results --net-num-nodes=10 --net-x-dim=100 --net-y-dim=100 --generate-random-network=True


#Sneeql variance testing -- first run (large network)