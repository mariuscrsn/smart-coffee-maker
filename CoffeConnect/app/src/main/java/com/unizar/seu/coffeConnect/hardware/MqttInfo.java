package com.unizar.seu.coffeConnect.hardware;

public class MqttInfo {
    private String serverUri;

    private String clientId;
    private String username;
    private String password;

    private String[] subscriptionTopics;
    private String publishTopic;

    public MqttInfo() {
        serverUri = "tcp://node02.myqtthub.com:1883";
        clientId = "usuario1";
        username = "usuario1";
        password = "usuario1";
        subscriptionTopics = new String[]{"STATUS/caf1"};
        publishTopic = "ACT/caf1/usuario1";
    }

    public MqttInfo(String serverUri, String clientId, String username, String password, String[] subscriptionTopics, String publishTopic) {
        this.serverUri = serverUri;
        this.clientId = clientId;
        this.username = username;
        this.password = password;
        this.subscriptionTopics = subscriptionTopics;
        this.publishTopic = publishTopic;
    }

    public String getServerUri() {
        return serverUri;
    }

    public String getClientId() {
        return clientId;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String[] getSubscriptionTopics() {
        return subscriptionTopics;
    }

    public String getPublishTopic() {
        return publishTopic;
    }
}
