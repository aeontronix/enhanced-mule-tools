# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## 1.1.10

### Added

- Interactive authentication only happens on-demand now
- Support for Exchange Labels
- Support for Exchange Categories
- Improved publish to exchanges and publish from exchange
- Publish exchange portal pages
- Ability to create deployment zip file for exchange-based deployments
- Added deprecated warnings when using legacy descriptors
- Access Token Support

### Removed

- Interactive Login (anypoint connected apps is too buggy, replaced by access tokens)

## 1.1.10

### Added

- Improvements to interactive authentication

## 1.1.8

### Fixed

- Interactive authentication isn't allowing downloading resources from exchange via maven

## 1.1.7

### Added

- Authentication using enhanced mule website

## 1.1.6

### Fixed

- anypoint.deploy.properties loads properties with incorrect keys

## 1.1.5

### Fixed

- Fixed bug where deployments fail when running without a pom (to deploy pre-compiled binaries)


## 1.1.4

### Added

- Allows individual properties to be passed into maven using -Danypoint.deploy.properties.*[propertyname]*

## 1.1.3

### Added

- Added RAML/OAS upload to exchange
- Allow to override API Asset creation from Deploy Mojo
- Checks roles permissions have been async deleted before re-assigning
- Allows individual properties to be passed into maven uding -Danypoint.deploy.properties.*[propertyname]*

### Changed

- When API asset version is not specified and it default from version, it now does not remove -SNAPSHOT

## 1.1.2

- Added org provisioning (cli only for now)
- Added org descriptor to markdown

## [1.1.0](https://gitlab.com/aeontronix/oss/enhanced-mule-tools/-/milestones/2)

### Breaking changes

- Goal 'prepare-deploy' is now 'process-descriptor'

### Changed

- Supports anypoint.yml or anypoint.yaml in addition to anypoint.json
- Changed deploy parameter structure to mirror that of mule's deploy parameters
- Added maven deploy property 'anypoint.deploy.propertyfile', allowing to specify a property file on deployment
- Automatically defaults api asset id and version based on pom
- API createClientApplication removed ( the client app will be created if `clientApp` json object exists )
- Supports bearer token
- Support creating HTTP exchange assets
- Supports provisioning-only (to use in combination with official mule plugin)
- Added "id" inside anypoint.json (defaults to artifactId). This is now used to generate client name, rather than api id.
- API `clientApp` has been renamed to `client` and moved to root
- descriptor 'access' is now inside "client"
