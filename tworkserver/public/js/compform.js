$(document).ready(function(){
  $("#primeinput").hide();
  $("#twork-comp-prime").click(function(){
    $("#primeinput").fadeIn(600,function(){});
  })})

$(document).on('change', '#fileinput', function() {
    var input = $(this);
    var numFiles = input.get(0).files ? input.get(0).files.length : 1;
    var message = (numFiles == 1) ? input.get(0).files[0].name : numFiles.toString().concat(" files");
        //label = input.val().replace(/\\/g, '/').replace(/.*\//, '');
    //input.trigger('fileselect', [numFiles, label]);
    $("#fileinputresponse").text(message.concat(" uploaded."));
});
