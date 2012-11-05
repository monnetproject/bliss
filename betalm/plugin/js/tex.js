(function(){

    var sections = document.querySelectorAll( 'span.tex' );

    for( var i = 0, len = sections.length; i < len; i++ ) {
        var section = sections[i];
        alert(tex);
        
        var tex = section.innerHTML;
        
        section.innerHTML = "<img src='http://mathurl.com/render.cgi?"+encodeURIComponent(tex)+"nocache'/>";
    }

})();
