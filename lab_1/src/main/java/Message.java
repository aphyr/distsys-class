import com.fasterxml.jackson.annotation.*;

public class Message {
    private Long id;
    private String src;
    private String dest;
    private Body body;

    public Long getId() {
        return id;
    }

    @JsonIgnore
    public void setId(final Long id) {
        this.id = id;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(final String src) {
        this.src = src;
    }

    public String getDest() {
        return dest;
    }

    public void setDest(final String dest) {
        this.dest = dest;
    }

    public Body getBody() {
        return body;
    }

    public void setBody(final Body body) {
        this.body = body;
    }

    public String toString() {
        return "(Msg " + src + " -> " + dest + " " + body.toString() +
                ")";
    }
}