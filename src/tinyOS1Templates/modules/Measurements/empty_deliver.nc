// used in experiments to generate cost parameters by eliminating deliver operator.
// __OPERATOR_DESCRIPTION__

__HEADER__

	command result_t DoTask.doTask()
  	{
  		call Child.requestData(1);
	   	return SUCCESS;
  	}

	event result_t Child.requestDataDone(__CHILD_TUPLE_PTR_TYPE__ _inQueue, int8_t _inHead, int8_t _inTail, uint8_t _inQueueSize)
	{
		return SUCCESS;
	}

	event result_t SendDeliver.sendDone(TOS_MsgPtr msg, result_t success)
	{
		return SUCCESS;
	}
}
