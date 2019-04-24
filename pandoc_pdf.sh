#!/bin/sh

pandoc --verbose --from=markdown_github --output=aphyr-distsys-intro.pdf --variable classoption=twocolumn --standalone README.markdown
