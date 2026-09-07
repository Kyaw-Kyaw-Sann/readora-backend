# Book Create and Update API Contract

This document defines the frontend contract for creating and updating books in the Readora Admin application.

## Common requirements

```text
Base URL: http://localhost:8080
Authentication: Bearer access token
Required role: ADMIN
Request content type: multipart/form-data
```

Do not manually set the `Content-Type` request header in a browser. The browser must generate the multipart boundary.

## TypeScript models

```ts
export type BookAccessType = "FREE" | "PREMIUM";
export type BookStatus = "DRAFT" | "PUBLISHED" | "ARCHIVED";

export interface CreateBookPayload {
  title: string;
  description?: string | null;
  isbn?: string | null;
  language?: string | null;
  publicationDate?: string | null; // YYYY-MM-DD
  author: string;
  pageCount?: number | null;
  audioDurationSeconds?: number | null;
  accessType: BookAccessType;
  categoryIds: number[];
}

export interface UpdateBookPayload extends CreateBookPayload {
  removeCover?: boolean;
  removePdf?: boolean;
  removeAudio?: boolean;
}

export interface BookFiles {
  cover?: File | null;
  pdf?: File | null;
  audio?: File | null;
}

export interface CategoryResponse {
  id: number;
  name: string;
  slug: string;
  description: string | null;
  active: boolean;
}

export interface BookDetailResponse {
  id: number;
  title: string;
  description: string | null;
  isbn: string | null;
  language: string | null;
  publicationDate: string | null;
  author: string;
  coverUrl: string | null;
  pdfUrl: string | null;
  audioUrl: string | null;
  pageCount: number | null;
  audioDurationSeconds: number | null;
  accessType: BookAccessType;
  status: BookStatus;
  viewCount: number;
  categories: CategoryResponse[];
  createdAt: string;
  updatedAt: string;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

export interface ValidationErrorResponse {
  success: false;
  message: "Validation failed";
  errors: Record<string, string>;
}
```

## Multipart fields

| Field | Content | Required |
|---|---|---:|
| `request` | JSON Blob with `application/json` type | Yes |
| `cover` | Cover image file | No |
| `pdf` | PDF file | No |
| `audio` | Audio file | No |

Field names are case-sensitive.

## Shared FormData builder

```ts
export function buildBookFormData(
  payload: CreateBookPayload | UpdateBookPayload,
  files: BookFiles
): FormData {
  const formData = new FormData();

  formData.append(
    "request",
    new Blob([JSON.stringify(payload)], {
      type: "application/json",
    })
  );

  if (files.cover) {
    formData.append("cover", files.cover);
  }

  if (files.pdf) {
    formData.append("pdf", files.pdf);
  }

  if (files.audio) {
    formData.append("audio", files.audio);
  }

  return formData;
}
```

Do not use this browser code:

```ts
// Wrong: the request part may be sent as text/plain.
formData.append("request", JSON.stringify(payload));
```

## Create a book

```http
POST /api/admin/books
Authorization: Bearer <access-token>
Content-Type: multipart/form-data; boundary=<generated-by-browser>
```

A newly created book always has the `DRAFT` status. Publishing is a separate operation.

### Create payload example

```ts
const payload: CreateBookPayload = {
  title: "Clean Code",
  description: "A handbook of agile software craftsmanship",
  isbn: "9780132350884",
  language: "English",
  publicationDate: "2008-08-01",
  author: "Robert C. Martin",
  pageCount: 464,
  audioDurationSeconds: null,
  accessType: "FREE",
  categoryIds: [1, 2],
};
```

### Create function

```ts
export async function createBook(
  payload: CreateBookPayload,
  files: BookFiles,
  accessToken: string
): Promise<BookDetailResponse> {
  const response = await fetch("http://localhost:8080/api/admin/books", {
    method: "POST",
    headers: {
      Authorization: `Bearer ${accessToken}`,
    },
    body: buildBookFormData(payload, files),
  });

  const result = await response.json();

  if (!response.ok) {
    throw new Error(result.message ?? "Failed to create book");
  }

  return result.data;
}
```

### Create success response

```text
HTTP 201 Created
```

```json
{
  "success": true,
  "message": "Book created successfully",
  "data": {
    "id": 9,
    "title": "Clean Code",
    "description": "A handbook of agile software craftsmanship",
    "isbn": "9780132350884",
    "language": "English",
    "publicationDate": "2008-08-01",
    "author": "Robert C. Martin",
    "coverUrl": "https://...",
    "pdfUrl": "https://...",
    "audioUrl": null,
    "pageCount": 464,
    "audioDurationSeconds": null,
    "accessType": "FREE",
    "status": "DRAFT",
    "viewCount": 0,
    "categories": [],
    "createdAt": "2026-09-07T16:45:00",
    "updatedAt": "2026-09-07T16:45:00"
  }
}
```

## Update a book

```http
PUT /api/admin/books/{id}
Authorization: Bearer <access-token>
Content-Type: multipart/form-data; boundary=<generated-by-browser>
```

This endpoint performs a full update, not a partial PATCH. Send all required book fields even if only one field changed.

### Update payload example

```ts
const payload: UpdateBookPayload = {
  title: "Clean Code - Updated",
  description: "Updated description",
  isbn: "9780132350884",
  language: "English",
  publicationDate: "2008-08-01",
  author: "Robert C. Martin",
  pageCount: 464,
  audioDurationSeconds: null,
  accessType: "FREE",
  categoryIds: [1, 2],
  removeCover: false,
  removePdf: false,
  removeAudio: false,
};
```

The three removal properties are optional. The backend treats omitted or `null` values as `false`. Sending explicit boolean values is recommended because it makes frontend intent clear.

### Update function

```ts
export async function updateBook(
  bookId: number,
  payload: UpdateBookPayload,
  files: BookFiles,
  accessToken: string
): Promise<BookDetailResponse> {
  const normalizedPayload: UpdateBookPayload = {
    ...payload,
    removeCover: payload.removeCover ?? false,
    removePdf: payload.removePdf ?? false,
    removeAudio: payload.removeAudio ?? false,
  };

  const response = await fetch(
    `http://localhost:8080/api/admin/books/${bookId}`,
    {
      method: "PUT",
      headers: {
        Authorization: `Bearer ${accessToken}`,
      },
      body: buildBookFormData(normalizedPayload, files),
    }
  );

  const result = await response.json();

  if (!response.ok) {
    throw new Error(result.message ?? "Failed to update book");
  }

  return result.data;
}
```

### Update success response

```text
HTTP 200 OK
```

```json
{
  "success": true,
  "message": "Book updated successfully",
  "data": {
    "id": 9,
    "title": "Clean Code - Updated",
    "status": "DRAFT"
  }
}
```

## Media update behavior

| New file | Remove flag | Result |
|---:|---:|---|
| No | `false` or omitted | Keep existing media |
| Yes | `false` or omitted | Replace existing media |
| No | `true` | Remove existing media |
| Yes | `true` | Reject with HTTP 400 |

For a published book:

- A cover must remain present.
- At least one of PDF or audio must remain present.
- At least one active category must remain assigned.

## Validation rules

| Field | Required | Rules |
|---|---:|---|
| `title` | Yes | Non-blank, maximum 200 characters |
| `description` | No | Blank values are stored as `null` |
| `isbn` | No | Maximum 50 characters; must be unique when supplied |
| `language` | No | Maximum 50 characters |
| `publicationDate` | No | ISO date: `YYYY-MM-DD` |
| `author` | Yes | Non-blank, maximum 150 characters |
| `pageCount` | No | Positive integer when supplied |
| `audioDurationSeconds` | No | Positive integer when supplied |
| `accessType` | Yes | `FREE` or `PREMIUM` |
| `categoryIds` | Yes | Non-empty active category IDs; duplicates are rejected |
| `removeCover` | Update only | Optional boolean; defaults to `false` |
| `removePdf` | Update only | Optional boolean; defaults to `false` |
| `removeAudio` | Update only | Optional boolean; defaults to `false` |

## File limits

| Field | Accepted content type | Maximum size |
|---|---|---:|
| `cover` | `image/*` | 10 MB |
| `pdf` | `application/pdf` | 100 MB |
| `audio` | `audio/*` | 200 MB |
| Entire multipart request | N/A | 350 MB |

## Error status codes

| Status | Meaning |
|---:|---|
| 400 | Invalid JSON, validation error, invalid category, duplicate ISBN, or conflicting media actions |
| 401 | Missing or invalid access token |
| 403 | Authenticated user does not have the ADMIN role |
| 404 | Book does not exist |
| 413 | File or complete multipart request is too large |
| 415 | The request is not multipart or the `request` part is not `application/json` |
| 500 | Unexpected server error |

### Business error response

```json
{
  "success": false,
  "message": "ISBN already exists",
  "data": null
}
```

### Validation error response

```json
{
  "success": false,
  "message": "Validation failed",
  "errors": {
    "title": "Title is required",
    "categoryIds": "At least one category is required"
  }
}
```

## Frontend checklist

- Use a valid ADMIN access token.
- Send the request as `multipart/form-data`.
- Add `request` as an `application/json` Blob.
- Do not manually set the multipart `Content-Type` header in browser code.
- Use the exact file field names: `cover`, `pdf`, and `audio`.
- Convert category IDs and numeric inputs to numbers before serializing.
- Send dates as `YYYY-MM-DD`.
- Send the complete required payload for PUT updates.
- Prefer explicit `false` values for all three media removal flags.
- Handle both standard error responses and validation error responses.
