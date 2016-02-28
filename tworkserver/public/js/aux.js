$(document).ready(function() {
  function loadNumber()
  {
    $(function() {
        $.get(
        "/page/activedev",
        function(data){
          $('#number-devices').text(data);
        });
    });
  }

  setInterval(loadNumber, 2000);


  $("#prime-submit").bind("click",function(){
    $.ajax({
      url: "/prime/".concat($("#input-number").val()),
      error: function() {
         $('#info').html('<p>An error has occurred</p>');
      },
      success: function(data) {
         $("#computation-result").text(data);
      },
      type: 'GET'
   });
  });
});
