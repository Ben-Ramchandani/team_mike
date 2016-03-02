@(computationID: String)


$(function() {
   var WS = window['MozWebSocket'] ? window['MozWebSocket'] : WebSocket;

   var socket = new WS('@routes.Display.socket(computationID).webSocketURL(request) ');

   var postImage = function(name) {

     var el = $('<img class="instantimage">');
     $(el).attr("src",name.data.concat(".jpg"));
     $("#files").append(el);
   }

   socket.onmessage = postImage;
});
