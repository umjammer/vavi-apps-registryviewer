[![Release](https://jitpack.io/v/umjammer/vavi-apps-registryviewer.svg)](https://jitpack.io/#umjammer/vavi-apps-registryviewer)
[![Java CI](https://github.com/umjammer/vavi-apps-registryviewer/actions/workflows/maven.yml/badge.svg)](https://github.com/umjammer/vavi-apps-registryviewer/actions/workflows/maven.yml)
[![CodeQL](https://github.com/umjammer/vavi-apps-registryviewer/actions/workflows/codeql-analysis.yml/badge.svg)](https://github.com/umjammer/vavi-apps-registryviewer/actions/workflows/codeql-analysis.yml)
![Java](https://img.shields.io/badge/Java-17-b07219)

# vavi-apps-registryviewer

🐣 Incubation for tree and application bindings.

## Install

 * [maven](https://jitpack.io/#umjammer/vavi-apps-registryviewer)

## Usage

```shell
 $ mvn -P run antrun:run -Dregistry='foo/bar/user.dat'
```

## References

 * https://github.com/libyal/libcreg/blob/main/documentation/Windows%209x%20Registry%20File%20(CREG)%20format.asciidoc
 * for nt
   * [vavi-nio-file-discutil](https://github.com/umjammer/vavi-nio-file-discutils) 
   * https://github.com/sarxos/win-registry

## TODO

 * apply [vav-apps-treeview](https://github.com/umjammer/vavi-apps-treeview)
 * ~~don't use serdes ... by profiling, groovy is slow, cache is working well~~
