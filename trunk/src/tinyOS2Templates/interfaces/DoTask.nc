
interface DoTask {

	command error_t doTask();
	event void doTaskDone(error_t err);

}


