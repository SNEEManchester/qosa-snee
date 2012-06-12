// used in experiments to generate cost parameters by eliminating tray
__HEADER__

	__TUPLE_TYPE__ tray[0][0];

	command result_t GetTuples.requestData(int16_t evalEpoch)
	{
		signal GetTuples.requestDataDone(tray[0], -1, -1, 0);
		return SUCCESS;
	}


	command result_t PutTuples.putTuples(__TUPLE_PTR_TYPE__ inQueue, int8_t inHead, int8_t inTail, uint8_t inQueueSize)
	{	
		call Leds.redOn();
		call Leds.redOff();
	}

	command result_t PutTuples.putPacket(__TUPLE_PTR_TYPE__ message)
	{
		return SUCCESS;
	}

}

