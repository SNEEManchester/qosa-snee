
__HEADER__

	command error_t DoTask.doTask()
  	{
		dbg("DBG_USR2","__MODULE_NAME__ doTask() entered, now listen to radio and place tuples in trays\n");

	   	return SUCCESS;
  	}


	event message_t* Receive.receive(message_t* msg, void* payload, uint8_t len)
	{
		if (len==sizeof(__MESSAGE_TYPE__))
		{
			__MESSAGE_PTR_TYPE__ packet = (__MESSAGE_PTR_TYPE__)payload;
			call PutTuples.putPacket(packet->tuples);
		}

		return msg;
	}


}



