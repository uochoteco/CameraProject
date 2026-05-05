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
    fetch('/start-camera')
        .then(response => {
            if (response.ok) {
                alert("Camera Launched!");
            }
        })
        .catch(err => console.error("Is the Java server running? ", err));
}