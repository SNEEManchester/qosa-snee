// __OPERATOR_DESCRIPTION__


__HEADER__

	#define OUT_QUEUE_CARD __OUT_QUEUE_CARD__

	__OUTPUT_TUPLE_TYPE__ outQueue[OUT_QUEUE_CARD];
	int8_t outHead;
	int8_t outTail;

  	__CHILD_TUPLE_PTR_TYPE__ inQueue;
	int8_t inHead;
	int8_t inTail;
	uint8_t inQueueSize;


	command error_t Parent.requestData(int32_t evalTime)
  	{
		dbg("DBG_USR2","__MODULE_NAME__ __OPERATOR_DESCRIPTION__ requestData() entered, now calling child\n");
		call Child.requestData(evalTime);
	   	return SUCCESS;
  	}


	void task signalDoneTask()
	{
		signal Parent.requestDataDone(outQueue, outHead, outTail, OUT_QUEUE_CARD);
	}


	void task outQueueAppendTask()
	{
		if (inHead >-1)
		{
			outHead=0;
			outTail=0;
			do
				{

				atomic
				{
					if __SELECT_PREDICATES__
					{
__CONSTRUCT_TUPLE__
						outTail=(outTail+1) % OUT_QUEUE_CARD;
					}

					inHead=((inHead+1)%inQueueSize);
				}
			} while(inHead != inTail);
		}
		else
		{
			outHead=-1;
			outTail=-1;
		}

		post signalDoneTask();
	}

	event void Child.requestDataDone(__CHILD_TUPLE_PTR_TYPE__ _inQueue, int8_t _inHead, int8_t _inTail, uint8_t _inQueueSize)
	{
		atomic
		{
			inQueue = _inQueue;
			inHead = _inHead;
			inTail = _inTail;
			inQueueSize = _inQueueSize;
		}

		post outQueueAppendTask();

		
	}


}
