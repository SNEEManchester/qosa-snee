// __OPERATOR_DESCRIPTION__


__HEADER__

	#define OUT_QUEUE_CARD __OUT_QUEUE_CARD__

	__CHILD_TUPLE_PTR_TYPE__ inQueue;
	int8_t inHead;
	int8_t inTail;
	uint16_t inQueueSize;
	int16_t currentEvalEpoch;

	__OUTPUT_TUPLE_TYPE__ outQueue[OUT_QUEUE_CARD];
	int8_t outHead;
	int8_t outTail;

	command result_t Parent.requestData(int16_t evalEpoch)
  	{
		dbg(DBG_USR2,"__MODULE_NAME__ __OPERATOR_DESCRIPTION__ requestData() entered, now calling child\n");
		atomic currentEvalEpoch = evalEpoch;
		call Child.requestData(evalEpoch);
	   	return SUCCESS;
  	}

	event result_t Child.requestDataDone(__CHILD_TUPLE_PTR_TYPE__ _inQueue, int8_t _inHead, int8_t _inTail, uint8_t _inQueueSize)
	{
		dbg(DBG_USR2,"__MODULE_NAME__ __OPERATOR_DESCRIPTION__ requestData() entered\n");
		atomic
		{
			inQueue = _inQueue;
			inHead = _inHead;
			inTail = _inTail;
			inQueueSize = _inQueueSize;
		}

		if (inHead >-1)
		{
			atomic
			{
				outHead=0;
				outTail=0;
			}
			do
				{

				atomic
				{
__CONSTRUCT_TUPLE__

					outTail= outTail+1;
					if (outTail == OUT_QUEUE_CARD) {
						outTail = 0;
					}
					inHead = inHead+1;
					if (inHead == inQueueSize) {
						inHead = 0;
					}
				}
			} while(inHead != inTail);
		}
		else
		{
			outHead=-1;
			outTail=-1;

		}
		signal Parent.requestDataDone(outQueue, outHead, outTail, OUT_QUEUE_CARD);
		return SUCCESS;
	}


}
