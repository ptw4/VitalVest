package coe.pitt.edu.vitalvest;
import android.os.Message;
/**
 * Created by DoctorProteus on 3/30/16.
 */
public class SystemMonitor {

    public KeyValueList processMsg(KeyValueList kvList) {
        Integer msgId = Integer.parseInt(kvList.getValue("MsgID"));
        KeyValueList kvResult;

        switch (msgId) {
            case 1: {
                if (vs == null) { // Information Packet
                    Acknowledgement ack = new Acknowledgement(1, false, "SystemMonitor");
                    kvResult = readMessage(ack);
                    return kvResult;
                } else {
                    Integer timeStamp = kvList.getValue("timeStamp");
                    Double sensorA = kvList.getValue("SensorA");
                    Double sensorB = kvList.getValue("SensorB");
                    kvResult = Packet(timeStamp, sensorA, sensorB);
                    return kvResult;
                }
            }
        }
    }

    public KeyValueList readMessage(Message message){
        Integer msgId = message.getMsgId();
        KeyValueList kvOut = new KeyValueList();

        switch(msgId) {
            case 26: {
                Acknowledgement msg = (Acknowledgement) message;
                kvOut.addPair("MsgID", msg.getAckID().toString());
                kvOut.addPair("Description", "General Acknowledgement");
                if(msg.getYesNo()) {
                    kvOut.addPair("YesNo", "Yes");
                } else {
                    kvOut.addPair("YesNo", "No");
                }
                kvOut.addPair("Name", msg.getName());
                return kvOut;
            }
            case 711: { // Acknowledge Vote
                AcknowledgeVote msg = (AcknowledgeVote) message;
                kvOut.addPair("MsgID", "711");
                kvOut.addPair("Status", msg.getStatus().toString());
                return kvOut;
            }
            case 712: { // Acknowledge RequestReport
                AcknowledgeRequestReport msg = (AcknowledgeRequestReport) message;
                String report = msg.getRankedReport();
                kvOut.addPair("MsgID", "712");
                kvOut.addPair("RankedReport", report);
                return kvOut;
            }
            default: {
                kvOut.addPair("MsgID", "21");
                kvOut.addPair("Description", "Acknowledgement (VotingSystem Created");
                kvOut.addPair("YesNo", "Yes");
                kvOut.addPair("Name", "SystemMonitor");
                return kvOut;
            }
        }

    }

}
