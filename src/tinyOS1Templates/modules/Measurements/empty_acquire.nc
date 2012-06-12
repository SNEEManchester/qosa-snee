// used in experiments to generate cost parameters by eliminating acquire.
// __OPERATOR_DESCRIPTION__

__HEADER__

	__OUTPUT_TUPLE_TYPE__ outQueue[0];
	command result_t Parent.requestData(int16_t evalEpoch)
  	{
		signal Parent.requestDataDone(outQueue, -1, -1, 0);
	   	return SUCCESS;
  	}

__GET_DATA_METHODS__

}