import java.util.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.annotation.*;

public class Body {
    public String type;
    public Long msg_id;
    public Long in_reply_to;
    private Map<String, Object> more = new HashMap();

    public void replaceMore(final Map<String, Object> more) {
        this.more = more;
    }

    @JsonAnySetter
    public void setMore(String key, Object value) {
        more.put(key, value);
    }

    @JsonAnyGetter
    public Map<String, Object> getMore() {
        return more;
    }


    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public Long getMsg_id() {
        return msg_id;
    }

    public void setMsg_id(final Long msg_id) {
        this.msg_id = msg_id;
    }

    public Long getIn_reply_to() {
        return in_reply_to;
    }

    public void setIn_reply_to(final Long in_reply_to) {
        this.in_reply_to = in_reply_to;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(Body ");
        sb.append(msg_id);
        if (null != in_reply_to) {
            sb.append(" (reply to ");
            sb.append(in_reply_to);
            sb.append(")");
        }
        sb.append(" ");
        sb.append(type);
        sb.append(" ");
        sb.append(more.toString());
        sb.append(")");
        return sb.toString();
    }
}