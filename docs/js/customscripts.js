// Detect small devices and move the TOC in line
function moveToc(){
    if(window.innerWidth < 1350){
        $( '#toc' ).detach().appendTo('#inline-toc').removeClass('position-fixed');
    } else {
        $( '#toc' ).detach().appendTo('.toc-col').addClass('position-fixed');
    }
}

$(document).ready(function () {
    // This handles the automatic toc. Use ## for subheads to auto-generate the on-page minitoc.
    // If you use html tags, you must supply an ID for the heading element in order for it to appear in the minitoc.
    $('#toc').toc({
        minimumHeaders: 0,
        listType: 'ul',
        showSpeed: 0,
        headers: 'h2,h3,h4',
    });

    // activate tooltips. although this is a bootstrap js function, it must be activated this way in your theme.
    $('[data-toggle="tooltip"]').tooltip({
        placement: 'top',
    });

    /**
     * AnchorJS
     */
    anchors.add('h2,h3,h4,h5');

    // Add an "Edit on GitHub" button to each header (except h1)
    let url = $('div.post-content').data('githubEditUrl');
    if ( url !== undefined ) {
        $('div.post-content')
            .find(':header:not(h1)')
            .append(
                '  <a class="edit-header" target="_blank" href=' +
                    url +
                    ' role="button">✏️️</a>'
            );
    }

    // Check if TOC needs to be moved on page load
    moveToc();

    // This highlights the active parent class in the navgoco sidebar. This is critical so that the parent expands
    // when you're viewing a page.
    // Note: the class needs to be added before navgoco is initialized. Navgoco uses then this information
    // to expand the menus.
    $( 'li.active' ).parents('li').toggleClass('active');

    // Initialize navgoco with default options
    $( '#mysidebar' ).navgoco({
        caretHtml: '',
        accordion: true,
        openClass: 'active',
        save: false,
        slide: {
            duration: 400,
            easing: 'swing'
        }
    });

    // Topnav toggle button for displaying/hiding nav sidebar
    $("#tg-sb-link").click(function(event) {
        $("#tg-sb-sidebar").toggle();
        $("#tg-sb-content").toggleClass('col-md-9');
        $("#tg-sb-content").toggleClass('col-md-12');
        $("#tg-sb-icon").toggleClass('fa-toggle-on');
        $("#tg-sb-icon").toggleClass('fa-toggle-off');
        event.preventDefault();
    });

    // PMD Versions Dropdown in topnav
    const versionsList = document.querySelector("#pmdVersions");
    const releasesRequest = new Request("https://api.github.com/repos/pmd/pmd/releases?per_page=5&page=1");
    window
        .fetch(releasesRequest)
        .then((response) => {
            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }
            return response.json();
        })
        .then((response) => response.map((r) => r.tag_name.replace('pmd_releases/', '')))
        .then((versions) => {
            versions.push('6.55.0');
            return versions.map((version) => {
                if (version.endsWith('-SNAPSHOT')) {
                    return `<a class="dropdown-item" href="https://docs.pmd-code.org/snapshot/" target="_blank">${version}</a>`;
                } else {
                    return `<a class="dropdown-item" href="https://docs.pmd-code.org/pmd-doc-${version}/" target="_blank">${version}</a>`;
                }
            });
        })
        .then((items) => {
            versionsList.innerHTML = items.join();
        })
        .catch((error) => {
            versionsList.innerHTML = "Couldn't download releases from api.github.com";
            console.log(error);
        });
});

// Check if TOC needs to be moved on window resizing
$(window).resize(function () {
    moveToc();
});
