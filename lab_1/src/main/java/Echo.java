import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static java.lang.System.in;

public class Echo {
    public static ObjectMapper mapper = new ObjectMapper();
    public static String nodeId;					// Our own node ID
    public static List<String> nodeIds;		// The other Node IDs in the cluster
    public static long nextMsgId = 0;			// The next message ID we'll emit

    static {
        mapper.readerFor(Message.class);
    }

    // Mainloop
    public static void main(String[] args) throws IOException {
        try {
            BufferedReader streamReader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String inputStr;
            while ((inputStr = streamReader.readLine()) != null) {
                System.err.println("Got message " + inputStr);
                Message msg = mapper.readValue(inputStr, Message.class);
                System.err.println("Parsed message" + msg);
                handleMsg(msg);
                System.err.println("Done with message.");
                System.err.flush();
            }
            System.err.println("Exiting normally.");
        } catch (Throwable t) {
            t.printStackTrace(System.err);
            t.printStackTrace(System.out);
            System.err.flush();
            System.out.flush();
            throw t;
        }
    }

    // Send a message body on stdout to the given destination node
    public static void sendMsg(final String dest, final Body msgBody) throws IOException {
        // Make a copy of body, assigning it a new message ID
        final Body body = new Body();
        body.setType(msgBody.getType());
        body.setMsg_id(nextMsgId++);
        body.setIn_reply_to(msgBody.getIn_reply_to());
        body.replaceMore(msgBody.getMore());
        // Construct a message to send
        final Message m = new Message();
        m.setSrc(nodeId);
        m.setDest(dest);
        m.setBody(body);
        // And print it out
        System.err.println("Sending " + dest + " " + body);
        System.out.println(mapper.writeValueAsString(m));;
        System.out.flush();
    }

    // Reply to a message with a response Body
    public static void reply(final Message requestMsg, final Body replyBody) throws IOException {
        System.err.println("Replying to " + requestMsg + " with " + replyBody);
        // Make a copy of body, filling in the in_reply_to field
        final Body body = new Body();
        body.setType(replyBody.getType());
        body.setIn_reply_to(requestMsg.getBody().getMsg_id());
        body.replaceMore(replyBody.getMore());
        // And send that reply back to the requester
        sendMsg(requestMsg.getSrc(), body);
    }

    // Handle a message from stdin
    public static void handleMsg(final Message msg) throws IOException {
        final Body body = msg.getBody();
        switch (body.getType()) {
            case "echo": handleEcho(msg);
                break;
            case "init": handleInit(msg);
                break;
            default: System.err.println("Don't know how to handle message of type " +
                    msg.getBody().getType() + " - " + msg);
                break;
        }
    }

    // Handle initialization message
    public static void handleInit(final Message req) throws IOException {
        nodeId  = (String)   			req.getBody().getMore().get("node_id");
        nodeIds = (List<String>)	req.getBody().getMore().get("node_ids");
        // And send back a response
        final Body res = new Body();
        res.setType("init_ok");
        reply(req, res);
    }

    // Handle echo message
    public static void handleEcho(final Message req) throws IOException {
        final Body res = new Body();
        res.setType("echo_ok");
        res.setMore("echo", req.getBody().getMore().get("echo"));
        reply(req, res);
    }
}