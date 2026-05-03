<!DOCTYPE html>
<html lang="en">
    <head>
    <meta charset="UTF-8">
    <title>Title</title>
<link rel="stylesheet" type="text/css" href="/style.css">
</head>
<body>
<div th:include="topBar :: content"></div>

<!-- Video Section -->
<div class="videoPlayerDiv">
    <video id="mainVideo" width="716" height="360" controls="controls" src="/video/1" preload="none" autoplay="autoplay">
        <source id="vidSrc" type="video/mp4">
        <!-- th:src="${'/video/' + movie.video}" -->
        <!-- Media Source Extensions!!!!!!!! -->
    </video>
</div>

<!-- Suggestions section -->

</body>
<script th:inline="javascript">
    let movie = /*[[${movie}]]*/ [];
    console.log(movie);
    console.log(movie.video);
    //document.getElementById("vidSrc").setAttribute("src", "/video/" + movie.video);

    const wait = (ms) => new Promise(resolve => setTimeout(resolve, ms));

    let videoElement = document.getElementById("mainVideo");
    if(!window.MediaSource){
    //error
}
    else{
    let media = new MediaSource();
    videoElement.src= URL.createObjectURL(media);
    media.addEventListener('sourceopen', srcOpen);
}

    async function srcOpen(e) {
    URL.revokeObjectURL(videoElement.src);
    let mime = 'video/mp4; codecs="avc1.4D401F, mp4a.40.2"';
    let media = e.target;

    let srcBuffer = media.addSourceBuffer(mime);
    //let targetURL = "/video/" + movie.video.toString();
    getMoov(srcBuffer);
    console.log(srcBuffer.mode);
    await wait(5000);
    getFragment(srcBuffer, 0, 180.01);
}

    function getMoov(srcBuffer){

    let data = {
    target: 'moov'
};

    fetch("/video/" + movie.video, {
    method: 'POST',
    headers: {
    'Content-Type': 'application/json',
},
    body: JSON.stringify(data)
}).then(response => response.arrayBuffer()).then(buffer => {

    let fragments = [];

    srcBuffer.appendBuffer(buffer);
});

}


    function getFragment(srcBuffer, startSec, endSec){

    let data = {
    target: 'frag',
    startSec: startSec,
    endSec: endSec
};

    fetch("/video/" + movie.video, {
    method: 'POST',
    headers: {
    'Content-Type': 'application/json',
},
    body: JSON.stringify(data)
}).then(response => response.arrayBuffer()).then(buffer => {
    console.log(buffer);
    srcBuffer.appendBuffer(buffer);
});

}


</script>
</html>