__HEADER__

	#define BUFFERING_FACTOR __BUFFERING_FACTOR__

	//vars shared for all trays (since we only process one at a time)
	int8_t bFactorCount;
	int16_t evalEpoch =  0;
	message_t packet;
	int8_t inHead;
	int8_t inTail;
	int8_t inQueueSize;

	int8_t tuplePacketPos;
	uint8_t pending=FALSE;


	//char timeBuf[128];

	__CHILD_TUPLE_PTR_TYPE__ inQueue;
	__MESSAGE_PTR_TYPE__ payload;


	command error_t DoTask.doTask()
  	{
		dbg("DBG_USR2","__MODULE_NAME__ doTask() entered as evalEpoch %d, now get tuples from trays and send them on\n",evalEpoch);
		dbg("DBG_USR2","RADIO start from __CURRENT_SITE_ID__ to __PARENT_ID__\n");

		bFactorCount = 0;
		tuplePacketPos = 0;

			//get the first tray
		call GetTuples.requestData(evalEpoch);
		return SUCCESS;
	}


	void task sendPacketDoneTask();

	void task sendPacketTask()
	{
		if (pending)
		{
			dbg("DBG_USR2", "Still pending");
			post sendPacketTask();
		}
		else
		{

			atomic pending = TRUE;
			//printTime(timeBuf, 128);
			//dbg("DBG_USR2","At %s Attempting to send __MESSAGE_TYPE__ packet to __PARENT_ID__\n",timeBuf);

			payload = (__MESSAGE_PTR_TYPE__)(call Packet.getPayload(&packet, NULL));

			//Pad any unsed tuples
			while (tuplePacketPos < __TUPLES_PER_PACKET__) // tuples per packet for source fragment type
			{
				payload->tuples[tuplePacketPos].evalEpoch = NULL_EVAL_EPOCH;
				tuplePacketPos++;
			}

			if (call AMSend.send(__PARENT_ID__,&packet, sizeof(__MESSAGE_TYPE__) )!=SUCCESS)
			{
				dbg("DBG_USR1","Error: Call to active message layer failed in __MODULE_NAME__\n");
				atomic pending = FALSE;
				//post sendPacketTask(); // retry sending packet (comment out accordingly)
				post sendPacketDoneTask(); // do not retry sending packet (comment out accordingly)
			}
			else
			{
				dbg("DBG_USR2", "Message accepted by active message layer in __MODULE_NAME__\n");
			}
		}

	}

	void task LoopControlTask()
	{
		//more tuples in the inQueue
		payload = (__MESSAGE_PTR_TYPE__)(call Packet.getPayload(&packet, NULL));
		if (call Packet.maxPayloadLength() < sizeof(__MESSAGE_PTR_TYPE__))
		{
			dbg("DBG_USR1","RADIO started but failed from __CURRENT_SITE_ID__ to __PARENT_ID__ as message size is greater\n");
			return;
      	}

		if (inHead >-1)
		{
			//generate tuples
			payload->tuples[tuplePacketPos] = inQueue[inHead];
			inHead = (inHead+1) % inQueueSize;
			if (inHead==inTail)
			{
				inHead = -1;
			}
			tuplePacketPos++;
		}

		if ((tuplePacketPos == __TUPLES_PER_PACKET__)) //current packet is full
		{
			post sendPacketTask();
		}
		else if (inHead >-1) // more tuples in inQueue
		{
			post LoopControlTask();
		}
		else if (bFactorCount < BUFFERING_FACTOR) // more data still buffered at other evaluation times
		{
			evalEpoch++;
			call GetTuples.requestData(evalEpoch);
		}

		else // no more data at other evaluation times
		{

			if (tuplePacketPos>0) // packet being constructed contains tuples, send
			{
				post sendPacketTask();
			}
			else
			{
				evalEpoch++;
				signal DoTask.doTaskDone(SUCCESS);

			}

		}
	}

	void task sendPacketDoneTask()
	{
		atomic pending = FALSE;
		tuplePacketPos=0;

		//more to send from this tray
		post LoopControlTask();

	}

	event void AMSend.sendDone(message_t* msg, error_t err)
	{
		atomic pending = FALSE;
		if (err==FAIL)
		{
			dbg("DBG_USR2","Sending failed, try resending\n");
			post sendPacketTask();
		}
		else if (err==SUCCESS)
		{
			dbg("DBG_USR2","Packet sent successfully\n");
			post sendPacketDoneTask();
		}
		else
		{
			dbg("DBG_USR2","Packet was camcelled\n");
		}


	}


	event void GetTuples.requestDataDone(__TUPLE_PTR_TYPE__  _inQueue, int8_t _inHead, int8_t _inTail, uint8_t _inQueueSize)
	{
		dbg("DBG_USR2", "tx:GetTuples.requestDataDone inHead=%d, inTail=%d, inQueueSize=%d\n", _inHead, _inTail, _inQueueSize);

		atomic
		{
			bFactorCount++;
			inQueue = _inQueue;
			inHead = _inHead;
			inTail = _inTail;
			inQueueSize = _inQueueSize;
		}

		post LoopControlTask();


	}

}
