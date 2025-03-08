let socket = new SockJS("/ws-logs");
let client = Stomp.over(socket);

client.connect({}, function(frame) {
    console.log("!!Connected");

    client.subscribe("/last/logs", function(data) {
        let logs = JSON.parse(data.body);
        if (Array.isArray(logs)) {
            logs.forEach(line => print(line));
        } else {
            print(logs);
        }
    });

    // Fetch initial logs after WebSocket connection is established
    fetch('/logs/initial')
        .then(response => response.json())  // Parse the JSON response
        .then(data => {
            console.log("Initial logs request successful", data);
            if (data.logs && Array.isArray(data.logs)) {
                data.logs.forEach(line => print(line));
            }
        })
        .catch(error => console.error("Error:", error));
});

function print(message) {
    let log = document.getElementById("logs");
    let newLog = document.createElement("p");

    newLog.textContent = message;
    log.appendChild(newLog);
}