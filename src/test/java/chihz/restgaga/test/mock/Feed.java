package chihz.restgaga.test.mock;


public class Feed {

    private final String feedId;

    private final String content;

    public Feed(String feedId, String content) {
        this.feedId = feedId;
        this.content = content;
    }

    public String getFeedId() {
        return this.feedId;
    }

    public String getContent() {
        return this.content;
    }
}
