// __OPERATOR_DESCRIPTION__

#include "itoa.h"
__HEADER__

	#define BUFFERING_FACTOR __BUFFERING_FACTOR__

	int32_t evalEpoch= 0;
	uint8_t bFactorCount=0;
	__CHILD_TUPLE_PTR_TYPE__ inQueue;
	int8_t inHead;
	int8_t inTail;
	uint8_t inQueueSize;

	char deliverStr[500];

	command error_t DoTask.doTask()
  	{
		dbg("DBG_USR2","__MODULE_NAME__ __OPERATOR_DESCRIPTION__ doTask() entered as evalEpoch %d, now call child\n",evalEpoch);
		dbg("DBG_USR1","DELIVER do task called\n");
		call Child.requestData(evalEpoch);
	   	return SUCCESS;
  	}

	event void SendDeliver.sendDone(message_t* msg, error_t error)
	{
	}

	void task deliverDataTask();

	void task sendToSerialPortTask()
	{
		post deliverDataTask();
	}

	void task deliverDataTask()
	{
		deliverStr[0] = '\0';
		if (inHead >-1)
		{
			char tmpStr[30];
			strcat(deliverStr, "DELIVER: (");
__CONSTRUCT_DELIVER_TUPLE_STR__
			strcat(deliverStr, ")\n");
			inHead= inHead+1;

			if (inHead == inQueueSize) {
			  inHead = 0;
		    }
		    if (inHead == inTail) {
		    	inHead = -1;
	    	}
	    	dbg("DBG_USR1", "%s", deliverStr);
	    	//sendPos = 0;
			post sendToSerialPortTask();
		}
		else
		{
			bFactorCount++;
			if (bFactorCount< BUFFERING_FACTOR)
			{
				evalEpoch++;
				call Child.requestData(evalEpoch);
			}
			else
			{
				evalEpoch++;
				bFactorCount = 0;
				signal DoTask.doTaskDone(SUCCESS);
			}
		}
	}


	event void Child.requestDataDone(__CHILD_TUPLE_PTR_TYPE__ _inQueue, int8_t _inHead, int8_t _inTail, uint8_t _inQueueSize)
	{
		dbg("DBG_USR2","__MODULE_NAME__ requestDataDone() signalled from child, delivering data\n" );

		atomic
		{
			inQueue = _inQueue;
			inHead = _inHead;
			inTail = _inTail;
			inQueueSize = _inQueueSize;
		}

		post deliverDataTask();


	}
}
