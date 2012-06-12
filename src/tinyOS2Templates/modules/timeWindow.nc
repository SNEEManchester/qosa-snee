// __OPERATOR_DESCRIPTION__


__HEADER__

	#define OUT_QUEUE_CARD __OUT_QUEUE_CARD__
	#define WINDOW_FROM __WINDOW_FROM__
	#define WINDOW_TO __WINDOW_TO__
	#define SLIDE __SLIDE__

	__OUTPUT_TUPLE_TYPE__ outQueue[OUT_QUEUE_CARD];
	int8_t outHead=-1;
	int8_t outTail=0;
	int32_t thisEvalTime=0;
	int32_t slideAdjust=0;
	int8_t windowHead=0;
	int8_t windowTail=0;
	int32_t originalEvalTimes[OUT_QUEUE_CARD];
	bool initialized = FALSE;

	__CHILD_TUPLE_PTR_TYPE__ inQueue;
	int8_t inHead;
	int8_t inTail;
	uint16_t inQueueSize;

	inline void initialize()
	{
		atomic
		{
			int i, j;
			for (i = 0; i < OUT_QUEUE_CARD; i++)
			{
				outQueue[i].evalTime = NULL_EVALTIME;
				originalEvalTimes[i] = NULL_EVALTIME;
			}

			initialized = TRUE;
		}
	}

	command error_t Parent.requestData(int32_t evalTime)
  	{
		dbg("DBG_USR2","__MODULE_NAME__ __OPERATOR_DESCRIPTION__ requestData() entered, now calling child\n");
		__MAX_DEBUG__ dbg("DBG_USR3","__MODULE_NAME__  start values thisEvalTime=%d, windowHead=%d, windowTail=%d, outHead=%d, outTail=%d\n",thisEvalTime, windowHead, windowTail, outHead, outTail);
		thisEvalTime=evalTime;
		call Child.requestData(evalTime);
	   	return SUCCESS;
  	}


	inline void setEvalTimes()
	{
		int8_t temp=0;
		if (outHead!=-1)
		{
			temp=outHead;
			outQueue[temp].evalTime= thisEvalTime;
			temp=(temp+1) % OUT_QUEUE_CARD;
			while(temp != outTail)
			{
				outQueue[temp].evalTime=thisEvalTime;
				temp=(temp+1) % OUT_QUEUE_CARD;
			}
		}
	}

	void task signalDoneTask()
	{
		__MAX_DEBUG__ dbg("DBG_USR3","__MODULE_NAME__  done with thisEvalTime=%d, windowHead=%d, windowTail=%d, outHead=%d, outTail=%d\n",thisEvalTime, windowHead, windowTail, outHead, outTail);
		signal Parent.requestDataDone(outQueue, windowHead, windowTail, OUT_QUEUE_CARD);
	}

	void task refreshWindowTask() {
		int32_t windowEvalTime=0;
		int8_t indexPos=0;

		windowHead = outHead;
		windowTail= outTail;

		// Moves the tail back to ignore the "to" tuples and tuples waiting for the next slide
		//CB: We express "TO" as a negative number when it is relative?
		windowEvalTime=thisEvalTime-slideAdjust + WINDOW_TO;
		indexPos	=((windowTail-1 + OUT_QUEUE_CARD) % OUT_QUEUE_CARD);
		__MAX_DEBUG__ dbg("DBG_USR3","__MODULE_NAME__  comparing indexPos %d with EvalTime %d to %d\n",indexPos,originalEvalTimes[indexPos],windowEvalTime);
		while ((windowHead >-1) && (originalEvalTimes[indexPos]>windowEvalTime) )
		{
			__MAX_DEBUG__ dbg("DBG_USR3","__MODULE_NAME__  indexPos %d with EvalTime %d ignored\n",indexPos,originalEvalTimes[indexPos]);
			windowTail=(windowTail-1 + OUT_QUEUE_CARD) % OUT_QUEUE_CARD;
			if (windowHead==windowTail)
			{
				windowHead=-1;//Empty
			}
			indexPos =((windowTail-1 + OUT_QUEUE_CARD) % OUT_QUEUE_CARD);
		}

		setEvalTimes();

		post signalDoneTask();
	}

	void task outQueueAppendTask()
	{
		if (inHead>-1)
		{
			do
				{
				atomic
				{
					// security Code
					if (outTail==outHead)
					{
						outHead=(outHead+1) % OUT_QUEUE_CARD;
						dbg("DBG_USR2","**** Overflow in __MODULE_NAME__ putTuple *****\n");
					}
				}

				atomic
				{
					if(inQueue[inHead].evalTime>=thisEvalTime + WINDOW_FROM - slideAdjust)
					{
						if (outHead == -1)
						{
							outHead=0;
							outTail=0;
						}

__CONSTRUCT_TUPLE__

						originalEvalTimes[outTail]=inQueue[inHead].evalTime;
						outTail=(outTail+1) % OUT_QUEUE_CARD;
						inHead=((inHead+1) % inQueueSize);
					}
					else
					{
					 	__MAX_DEBUG__ dbg("DBG_USR3","__MODULE_NAME__  not appending tuple with evalTime %d ignored\n",inQueue[inHead]);
				 	}

				}

			} while(inHead != inTail);
		}

		__MAX_DEBUG__ dbg("DBG_USR3","__MODULE_NAME__  after append thisEvalTime=%d, windowHead=%d, windowTail=%d, outHead=%d, outTail=%d\n",thisEvalTime, windowHead, windowTail, outHead, outTail);
		post refreshWindowTask();
	}

	void task removeExpiredTuplesTask()
	{
		//while ((outHead >-1)&& (outQueue[outHead].evalTime<=thisEvalTime-__WINDOW_FROM__-slideAdjust) )
		//CB: We express WINDOW_FROM as a negative inclusive number when it is relative
		while ((outHead >-1)&& (originalEvalTimes[outHead]<=thisEvalTime + WINDOW_FROM -slideAdjust) )
		{
			outHead=(outHead+1) % OUT_QUEUE_CARD;
			if (outHead==outTail)
			{
				outHead=-1;
			}
		}
		__MAX_DEBUG__ dbg("DBG_USR3","__MODULE_NAME__  after remove thisEvalTime=%d, windowHead=%d, windowTail=%d, outHead=%d, outTail=%d, slideAdjust=%d\n",thisEvalTime, windowHead, windowTail, outHead, outTail,slideAdjust);

		post outQueueAppendTask();
	}

	event void Child.requestDataDone(__CHILD_TUPLE_PTR_TYPE__ _inQueue, int8_t _inHead, int8_t _inTail, uint8_t _inQueueSize)
	{
		dbg("DBG_USR2","__MODULE_NAME__ requestDataDone() signalled  child\n");

		atomic
		{
			inQueue = _inQueue;
			inHead = _inHead;
			inTail = _inTail;
			inQueueSize = _inQueueSize;

			slideAdjust=thisEvalTime % SLIDE;
		}
		post removeExpiredTuplesTask();

		
	}



}
