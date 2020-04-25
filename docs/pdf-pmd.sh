#!/usr/bin/env bash

echo "Building PDF-friendly HTML site for PMD ..."
bundle exec jekyll serve --detach --config _config.yml,pdfconfigs/config_pmd_pdf.yml
echo "done"

echo "Building the PDF ...";
prince --javascript --input-list=_site/prince-list.txt -o pmd.pdf
echo "done";

pkill -f jekyll

