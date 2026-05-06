function onStart() {
    document.getElementById('Instructions').style.display = 'none';
    document.getElementById('main-menu').style.display = 'block';
}

function showInstructions() {
    document.getElementById('main-menu').style.display = 'none';
    document.getElementById('Instructions').style.display = 'block';
}

function showMainMenu() {
    document.getElementById('Instructions').style.display = 'none';
    document.getElementById('main-menu').style.display = 'block';
}

function launchJavaApp() {
    console.log("Contacting Java server...");
    fetch("http://localhost:9090/start-camera").then(response => {
            if (response.ok) {
                alert("Camera Launched!");
            }
        }).catch(err => console.error("Is working?", err));
}