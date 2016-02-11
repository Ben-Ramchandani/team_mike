$(document).ready(function() {
  $("#prime-submit").click(function(){
    $.ajax({
      url:'/prime/' + $("#input-number").val();
    }).done(function(data){
      $("#computation-result").val(data);
    })
  })
})
