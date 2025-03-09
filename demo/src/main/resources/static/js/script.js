let socket = new SockJS("/ws-logs");
let client = Stomp.over(socket);

client.connect({}, function(frame) {
    console.log("Connected" + frame);

    client.subscribe("/last/logs", function(data) {
        let logs = JSON.parse(data.body);
        if (Array.isArray(logs)) {
            logs.forEach(line => print(line));
        } else {
            print(logs);
        }
    });

    // Fetch initial logs after WebSocket connection is established
    client.send("/app/subscribe", {}, {});
});

function print(message) {
    let log = document.getElementById("logs");
    let newLog = document.createElement("p");

    newLog.textContent = message;
    log.appendChild(newLog);
}