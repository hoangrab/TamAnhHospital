export type apiResponse<T> = {
    status: String,
    message: String,
    data: T;
}