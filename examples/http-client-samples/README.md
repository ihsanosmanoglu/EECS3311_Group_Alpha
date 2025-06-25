# HTTP Client Examples

This directory contains reference code for HTTP client implementations.

## Files:

### GetData.java
- Original example from `http-test-code` project
- Demonstrates making HTTP calls to external APIs
- Shows JSON parsing with Gson library
- Example of calling World Bank API for population data

### HttpClient.java  
- Example from `httpCall` project
- Shows HTTP POST requests to local server
- Client-server communication pattern

## Purpose:

These examples serve as reference implementations for:
- Making HTTP calls to external nutrition APIs
- JSON data parsing and processing
- Error handling in network requests
- Integration patterns for external data sources

## Usage in Project:

The patterns shown here can be adapted for:
- `ExternalAdapter` implementation
- `CNFDataClient` for API-based nutrition data
- Future REST API integrations
- Background data synchronization

## Note:

These are reference implementations only. The actual project uses the adapter pattern to abstract external data access. 