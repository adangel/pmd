---
title: Apex support
permalink: pmd_languages_apex.html
last_updated: July 2026 (7.27.0)
author: Clément Fournier
tags: [languages, PmdCapableLanguage, CpdCapableLanguage]
summary: "Apex-specific features and guidance"
---

> [Apex](https://developer.salesforce.com/docs/atlas.en-us.apexcode.meta/apexcode/apex_dev_guide.htm) is a strongly
> typed, object-oriented programming language that allows developers to execute flow and
> transaction control statements on the Salesforce Platform server, in conjunction with calls to the API.

{% include language_info.html name='Apex' id='apex' implementation='apex::lang.apex.ApexLanguageModule' supports_pmd=true supports_cpd=true since='5.5.0' %}

## Metrics framework

In order to use code metrics in Apex, use the metrics constants in {% jdoc apex::lang.apex.metrics.ApexMetrics %},
together with {% jdoc core::lang.metrics.MetricsUtil %}.

## Multifile Analysis

Integration happens in {% jdoc apex::lang.apex.multifile.ApexMultifileAnalysis %}. It uses
[Apex Language Server](https://github.com/apex-dev-tools/apex-ls). For detailed information, see also [Apexlink POC #2830](https://github.com/pmd/pmd/pull/2830).
This feature requires the language property/environment variable `PMD_APEX_ROOT_DIRECTORY`.

Used for rules {% rule apex/design/UnusedMethod %} and {% rule apex/errorprone/AvoidInterfaceAsMapKey %}.

## Language Properties

See [Apex language properties](pmd_languages_configuration.html#apex-language-properties)

## Java Platform Module System

`pmd-apex` is an *automatic* module: its jar manifest declares
`Automatic-Module-Name: net.sourceforge.pmd.lang.apex`, but it ships no module descriptor. You can
`requires net.sourceforge.pmd.lang.apex;` from your own named module, but **apex-ls and its scala
dependencies must be placed on the classpath, not on the module path**.

The reason is that no valid automatic module name can be derived from their file names -
`apex-ls_2.13-6.2.0.jar` yields `apex.ls.2.13`, and `2` is not a Java identifier - and they do not
declare an `Automatic-Module-Name`. Such a jar on `--module-path` makes the JVM fail at startup with
`FindException: Unable to derive module descriptor`, even if no module requires it. An automatic
module like `pmd-apex` can read the classpath; a named module could not.

## Parser

Since PMD 7.0.0 we use the open source [apex-parser](https://github.com/apex-dev-tools/apex-parser),
together with [Summit AST](https://github.com/adangel/summit-ast) which translates the ANTLR parse tree
into an AST.

When PMD added Apex support with version 5.5.0, it utilized the Apex Jorje library to parse Apex source
and generate an AST. This library is however a binary-blob provided as part of the
[Salesforce Extensions for VS Code](https://github.com/forcedotcom/salesforcedx-vscode), and it is closed-source.
