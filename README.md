# Tickify

Tickify is a tertiary event ticketing platform for South African universities and colleges. It is built as a Java EE (JEE) web application and organized into three connected components:

1. Admin Console
2. Client Site
3. Security Scanner

## Overview

Tickify helps campus event teams publish and manage events, while students and external attendees can discover events and buy digital tickets. Each ticket includes a unique QR code used at venue entrances for fast and secure verification.

The platform supports institutions such as UJ, Wits, UP, and UCT, and can enforce student-only event access using institutional email-domain validation.

## Core Components

### 1. Admin Console

Administrators can:

- Create and manage campus events
- Configure student-only restrictions
- Track ticket sales and attendance
- Monitor revenue through a real-time dashboard

### 2. Client Site

Students and external attendees can:

- Browse upcoming campus events
- Register using university credentials
- Purchase event tickets
- Receive unique QR-coded tickets for entry

### 3. Security Scanner

Venue guards use a mobile-first web interface to:

- Scan ticket QR codes via camera
- Validate tickets through manual code entry (fallback)
- Receive instant validation feedback (visual, audio, and vibration)

## Java EE Architecture

This system follows a layered Java EE structure:

- Presentation layer: JSP pages and servlet controllers
- Business and data-access layer: DAO classes for entity operations
- Persistence layer: JPA entities and persistence configuration

All client applications communicate with the backend through REST-style API calls. Authentication state is maintained on the client using localStorage session tokens.

## Data Storage and Initialization

The datastore layer supports:

- Local JSON file storage
- Oracle database connectivity

On first launch, the server seeds default administrator and security accounts to simplify initial setup.

## Project Context

This repository contains the Java EE implementation for the web platform, including servlets, DAO classes, entities, and JSP-based views for the different user roles.

## Branding

This project uses the Tickify brand name across the application and documentation.

## Production Hardening

Use [PRODUCTION_HARDENING_CHECKLIST.md](PRODUCTION_HARDENING_CHECKLIST.md) as the release gate checklist for security, validation, scanner readiness, client readiness, admin readiness, and deployment operations.
