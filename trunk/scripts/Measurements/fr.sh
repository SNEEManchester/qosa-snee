#Empty
#python scripts/Measurements/FullRepeater.py --simulation-Duration=5000 --ouput-root="c:/measurements" --measurement-dir="Empty/1Value5000/Noposts" --nesc-input-directory="/cygdrive/c/measurements/code/emptyNoPosts1Value"

#python scripts/Measurements/FullRepeater.py --simulation-Duration=5000 --ouput-root="c:/measurements" --measurement-dir="Empty/1Value5000/with1Post" --nesc-input-directory="/cygdrive/c/measurements/code/emptywith1Post1Value"

#python scripts/Measurements/FullRepeater.py --simulation-Duration=5000 --ouput-root="c:/measurements" --measurement-dir="Empty/1Value5000/with2Posts" --nesc-input-directory="/cygdrive/c/measurements/code/emptyWith2Posts1Value"

#python scripts/Measurements/FullRepeater.py --simulation-Duration=5000 --ouput-root="c:/measurements" --measurement-dir="Empty/1Value5000/with1RadioTimer" --nesc-input-directory="/cygdrive/c/measurements/code/emptyWith1RadioTimer1Value"

#python scripts/Measurements/FullRepeater.py --simulation-Duration=2000 --ouput-root="c:/measurements" --measurement-dir="Empty/1Value2000/withRadioTimer" --nesc-input-directory="/cygdrive/c/measurements/code/emptyWithRadioTimer1Value"

#python scripts/Measurements/FullRepeater.py --simulation-Duration=3000 --ouput-root="c:/measurements" --measurement-dir="Empty/1Value3000/withRadioTimer" --nesc-input-directory="/cygdrive/c/measurements/code/emptyWithRadioTimer1Value"

#python scripts/Measurements/FullRepeater.py --simulation-Duration=4000 --ouput-root="c:/measurements" --measurement-dir="Empty/1Value4000/withRadioTimer" --nesc-input-directory="/cygdrive/c/measurements/code/emptyWithRadioTimer1Value"

#Acquire
#python scripts/Measurements/FullRepeater.py --schema-file=input/measurements/1Site-1Source-schemas.xml  --simulation-Duration=2000 --qos-acquisition-interval=5000 --measurements-remove-operators="trayAlldeliverAll" --query-dir="input/measurements" --query=1Values --measurement-dir="acquire/1Values2000"

#python scripts/Measurements/FullRepeater.py --schema-file=input/measurements/1Site-1Source-schemas.xml  --simulation-Duration=3000 --qos-acquisition-interval=5000 --measurements-remove-operators="trayAlldeliverAll" --query-dir="input/measurements" --query=1Values --measurement-dir="acquire/1Values3000"
 
#python scripts/Measurements/FullRepeater.py --schema-file=input/measurements/1Site-1Source-schemas.xml  --simulation-Duration=4000 --qos-acquisition-interval=5000 --measurements-remove-operators="trayAlldeliverAll" --query-dir="input/measurements" --query=1Values --measurement-dir="acquire/1Values4000"

#Tray
python scripts/Measurements/FullRepeater.py --schema-file=input/measurements/1Site-1Source-schemas.xml  --simulation-Duration=1000 --qos-acquisition-interval=5000 --measurements-remove-operators="trayAlldeliverAll" --query-dir="input/measurements" --query=1Values --measurement-dir="Tray_in/1Values2000"

python scripts/Measurements/FullRepeater.py --schema-file=input/measurements/1Site-1Source-schemas.xml  --simulation-Duration=2000 --qos-acquisition-interval=5000 --measurements-remove-operators="trayAlldeliverAll" --query-dir="input/measurements" --query=1Values --measurement-dir="Tray_in/1Values2000"

python scripts/Measurements/FullRepeater.py --schema-file=input/measurements/1Site-1Source-schemas.xml  --simulation-Duration=3000 --qos-acquisition-interval=5000 --measurements-remove-operators="trayAlldeliverAll" --query-dir="input/measurements" --query=1Values --measurement-dir="Tray_in/1Values3000"
 
python scripts/Measurements/FullRepeater.py --schema-file=input/measurements/1Site-1Source-schemas.xml  --simulation-Duration=4000 --qos-acquisition-interval=5000 --measurements-remove-operators="trayAlldeliverAll" --query-dir="input/measurements" --query=1Values --measurement-dir="Tray_in/1Values4000"

python scripts/Measurements/FullRepeater.py --schema-file=input/measurements/1Site-1Source-schemas.xml  --simulation-Duration=5000 --qos-acquisition-interval=5000 --measurements-remove-operators="trayAlldeliverAll" --query-dir="input/measurements" --query=1Values --measurement-dir="Tray_in/1Values5000"

#python scripts/Measurements/FullRepeater.py --schema-file=input/measurements/1Site-1Source-schemas.xml  --simulation-Duration=500 --qos-acquisition-interval=5000 --query=/measurements/2Values --measurement-dir="full500_2Values"

#python scripts/Measurements/FullRepeater.py --schema-file=input/measurements/1Site-1Source-schemas.xml  --simulation-Duration=500 --qos-acquisition-interval=5000 --query=/measurements/3Values --measurement-dir="full500_3Values"

#python scripts/Measurements/FullRepeater.py --schema-file=input/measurements/1Site-1Source-schemas.xml  --simulation-Duration=500 --qos-acquisition-interval=5000 --query=/measurements/1Values-id --measurement-dir="full500_1Values-id"

#python scripts/Measurements/FullRepeater.py --schema-file=input/measurements/1Site-1Source-schemas.xml  --simulation-Duration=500 --qos-acquisition-interval=5000 --query=/measurements/2Values-id --measurement-dir="full500_2Values-id"

#python scripts/Measurements/FullRepeater.py --schema-file=input/measurements/1Site-1Source-schemas.xml  --simulation-Duration=500 --qos-acquisition-interval=5000 --query=/measurements/3Value-ids --measurement-dir="full500_3Values-id"

#python scripts/Measurements/FullRepeater.py --schema-file=input/measurements/1Site-1Source-schemas.xml  --simulation-Duration=500 --qos-acquisition-interval=5000 --query=/measurements/1Values-time --measurement-dir="full500_1Values-time"

#python scripts/Measurements/FullRepeater.py --schema-file=input/measurements/1Site-1Source-schemas.xml  --simulation-Duration=500 --qos-acquisition-interval=5000 --query=/measurements/2Values-time --measurement-dir="full500_2Values-time"

#python scripts/Measurements/FullRepeater.py --schema-file=input/measurements/1Site-1Source-schemas.xml  --simulation-Duration=500 --qos-acquisition-interval=5000 --query=/measurements/3Values-time --measurement-dir="full500_3Values-time"

#python scripts/Measurements/FullRepeater.py --schema-file=input/measurements/1Site-1Source-schemas.xml  --simulation-Duration=500 --qos-acquisition-interval=5000 --query=/measurements/1Values-id-time --measurement-dir="full500_1Values-id-time"

#python scripts/Measurements/FullRepeater.py --schema-file=input/measurements/1Site-1Source-schemas.xml  --simulation-Duration=500 --qos-acquisition-interval=5000 --query=/measurements/2Values-id-time --measurement-dir="full500_2Values-id-time"

#python scripts/Measurements/FullRepeater.py --schema-file=input/measurements/1Site-1Source-schemas.xml  --simulation-Duration=500 --qos-acquisition-interval=5000 --query=/measurements/3Values-id-time --measurement-dir="full500_3Values-id-time"

#python scripts/Measurements/FullRepeater.py --nesc-input-directory=/cygdrive/c/measurements/code/3Values-id-time --simulation-Duration=100 measurement-dir="full" 

#python scripts/Measurements/FullRepeater.py --nesc-input-directory=/cygdrive/c/tmp/avrora --simulation-Duration=200 measurement-dir="sim200"

#python scripts/Measurements/FullRepeater.py --nesc-input-directory=/cygdrive/c/tmp/avrora --simulation-Duration=300 measurement-dir="sim300"

#python scripts/Measurements/FullRepeater.py --nesc-input-directory=/cygdrive/c/tmp/avrora --simulation-Duration=400 measurement-dir="sim400"

#python scripts/Measurements/FullRepeater.py --nesc-input-directory=/cygdrive/c/tmp/avrora --simulation-Duration=500 measurement-dir="sim500"
