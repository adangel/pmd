# Building PMD doc

## Prerequisites

Under Debian/Ubuntu:

    apt install python-sphinx python-pip
    apt pip install sphinx_rtd_theme

## Build it

    make rtd

If you don't have the [readthedocs theme](https://github.com/rtfd/sphinx_rtd_theme) installed, you can just build the html with the default theme:

    make html

