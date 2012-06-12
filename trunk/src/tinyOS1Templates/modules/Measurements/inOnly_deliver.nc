// __OPERATOR_DESCRIPTION__

#include "String.h"

__HEADER__

	#define BUFFERING_FACTOR __BUFFERING_FACTOR__

	int16_t evalEpoch= 0;
	uint8_t bFactorCount=0;
	__CHILD_TUPLE_PTR_TYPE__ inQueue;
	int8_t inHead;
	int8_t inTail;
	uint8_t inQueueSize;

	//used for writing to serial port
	int sendPos;

	command result_t DoTask.doTask()
  	{
		//__GET_TRAY_OUT_EXPERIMENT__On()
		call Child.requestData(evalEpoch);
	   	return SUCCESS;
  	}

	void task sendToSerialPortTask();

	event result_t SendDeliver.sendDone(TOS_MsgPtr msg, result_t success)
	{
		//__GET_DELIVER_TIMES_EXPERIMENT__Off();
		sendPos += DELIVER_PAYLOAD_SIZE;
		post sendToSerialPortTask();

		return SUCCESS;
	}

	event result_t Child.requestDataDone(__CHILD_TUPLE_PTR_TYPE__ _inQueue, int8_t _inHead, int8_t _inTail, uint8_t _inQueueSize)
	{

		//__GET_TRAY_OUT_EXPERIMENT__Off()
		atomic
		{
			inQueue = _inQueue;
			inHead = _inHead;
			inTail = _inTail;
			inQueueSize = _inQueueSize;
		}
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
		}

		return SUCCESS;
	}



}
