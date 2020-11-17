---
layout: default
title: Authentication
parent: Maven Plugin
grand_parent: Reference Docs
permalink: /refdocs/maven/authentication.html
nav_order: 1
---
# Authentication

For any goal marked as 'authenticated' below, you will need to specify one of the following authentication credentials

## Username / Password

If you have a user with username/password credentials (so NOT a Single Sign On user), you can just specify the following properties:

| Name | Property | Description |
|------|----------|-------------|
| username | anypoint.username | Anypoint Username
| password | anypoint.password | Anypoint Password

## Access Token

You can obtain an access token by logging on to https://www.enhanced-mule.com, and in the profile (click on the user
icon top right), enable offline access and create a user token.

| Name | Property | Description |
|------|----------|-------------|
| emAccessTokenId | emule.accesstoken.id | Access Token Id
| emAccessTokenSecret | emule.accesstoken.secret | Access Token Secret

## Connected Apps

You can also use a "connected apps" client id/secret (grant type must be `client_credentials`)

**IMPORTANT: At the time of this documentation being updated, there was a bug with connected apps client credential,
which causes client application create impossible**

| Name | Property | Description |
|------|----------|-------------|
| clientId | anypoint.client.id | Anypoint authentication client id
| clientSecret | anypoint.client.secret | Anypoint authentication client secret

## Bearer Token

Alternatively you can obtain a user's authentication bearer token and pass it using the following attribute

| Name | Property | Description |
|------|----------|-------------|
| bearer | anypoint.bearer | Anypoint authentication bearer token
