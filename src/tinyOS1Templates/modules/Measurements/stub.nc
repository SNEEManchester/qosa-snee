// __OPERATOR_DESCRIPTION__

__HEADER__

	#define OUT_QUEUE_CARD __OUT_QUEUE_CARD__

	__CHILD_TUPLE_PTR_TYPE__ inQueue;
	int8_t inHead;
	int8_t inTail;
	uint16_t inQueueSize;
	int16_t currentEvalEpoch;

	__OUTPUT_TUPLE_TYPE__ outQueue[OUT_QUEUE_CARD];

	command result_t Parent.requestData(int16_t evalEpoch)
  	{
		//__GET_STUB_TIMES__On();
		currentEvalEpoch = evalEpoch;
		call Child.requestData(evalEpoch);
	   	return SUCCESS;
  	}
  	
  	event result_t Child.requestDataDone(__CHILD_TUPLE_PTR_TYPE__ _inQueue, int8_t _inHead, int8_t _inTail, uint8_t _inQueueSize)
	{
		//__GET_STUB_TIMES__Off();
		signal Parent.requestDataDone(outQueue, -1, -1, 1);
		return SUCCESS;
	}
  	
}
