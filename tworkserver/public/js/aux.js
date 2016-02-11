$(document).ready(function() {
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
