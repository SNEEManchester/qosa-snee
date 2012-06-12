// __OPERATOR_DESCRIPTION__

__HEADER__

	#define OUT_QUEUE_CARD 1

	__OUTPUT_TUPLE_TYPE__ outQueue[OUT_QUEUE_CARD];
__READING_VAR_DECLS__
  	int8_t outHead=-1;
	int8_t outTail=0;
	int16_t currentEvalEpoch;

	command error_t Parent.requestData(int16_t evalEpoch)
  	{
		dbg("DBG_USR2","__MODULE_NAME__ __OPERATOR_DESCRIPTION__ requestData() entered, now acquire data\n");
		atomic currentEvalEpoch = evalEpoch;
		//dbg("DBG_USR1","ACQUIRE: Mote %d REQUESTED at %lld \n", NODE_NUM,tos_state.tos_time);
		atomic
		{
			acquiring0 = TRUE;
		}

		call Read0.read();
	   	return SUCCESS;
  	}



	void task signalDoneTask()
	{
		dbg("DBG_USR2","__MODULE_NAME__ __OPERATOR_DESCRIPTION__ requestDataDone() entered\n");
		signal Parent.requestDataDone(outQueue, outHead, outTail, OUT_QUEUE_CARD);
	}



	void task constructTupleTask()
	{
		//__GET_ACQUIRE_TIMES_EXPERIMENT__Off();
		dbg("DBG_USR3","__MODULE_NAME__ __OPERATOR_DESCRIPTION__ constructTupleTask entered\n");
		atomic
		{
			if (__FULL_ACQUIRE_PREDICATES__)
			{
				outHead=0;
				outTail=0;
__CONSTRUCT_TUPLE__
			}
			else
			{
				outHead=-1;
				dbg("DBG_USR2","Tuple deleted due to predicate __ACQUIRE_PREDICATES__\n");
			}

		}

		post signalDoneTask();
	}

__GET_DATA_METHODS__

}
