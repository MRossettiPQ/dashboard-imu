import type { AxiosResponse } from 'axios';
import type { Pagination } from 'src/common/models/models';

export default class PaginationUtils<T, R extends { page: number; rpp: number }> {
  constructor({
    service,
    params,
    onError,
    onSuccess,
    onLoad,
  }: {
    service: (params: R) => Promise<AxiosResponse<Pagination<T>>>;
    params: R;
    onError?: (error: unknown) => void;
    onSuccess?: (data: Pagination<T>) => void;
    onLoad?: (data: Pagination<T>) => void;
  }) {
    this.service = service;
    this.params = params;
    this.onError = onError;
    this.onSuccess = onSuccess;
    this.onLoad = onLoad;
  }

  service: (params: R) => Promise<AxiosResponse<Pagination<T>>>;
  result: Pagination<T> = {
    list: [],
    page: 1,
    pageCount: 0,
    count: 0,
    rpp: 0,
  };
  params: R;
  loading: boolean = false;
  infinite: boolean = false;
  loaded: boolean = false;
  error: boolean = false;
  controller: AbortController = new AbortController();
  status: number = -1;
  items: T[] = [];

  private readonly onError?: ((error: unknown) => void) | undefined;
  private readonly onSuccess?: ((data: Pagination<T>) => void) | undefined;
  private readonly onLoad?: ((data: Pagination<T>) => void) | undefined;

  abortRequest(): void {
    if (this.loading) {
      this.controller.abort();
    }
  }

  get isEmpty(): boolean {
    return this.result?.list?.length === 0;
  }

  get currentPage(): number {
    return this.result?.page ?? 1;
  }

  get totalPages(): number {
    return this.result?.pageCount ?? 0;
  }

  get totalItems(): number {
    return this.result?.count ?? 0;
  }

  get hasNext(): boolean {
    return this.totalPages > this.currentPage;
  }

  get hasPrev(): boolean {
    return this.totalPages < this.currentPage;
  }

  get hasMore(): boolean {
    return this.result.page < this.result.pageCount;
  }

  async loadNext(): Promise<void> {
    if (!this.hasMore) {
      return;
    }
    this.params.page += 1;
    await this.search();
  }

  async loadPrev(): Promise<void> {
    if (this.params.page < 2) {
      return;
    }
    this.params.page -= 1;
    await this.search();
  }

  async loadMore(): Promise<void> {
    if (!this.hasMore) {
      return;
    }
    this.params.page += 1;
    return this.search();
  }

  get hasError(): boolean {
    return this.error;
  }

  async search() {
    const signal = this.controller.signal;
    try {
      this.loading = true;
      if (!this.infinite) {
        this.result = {
          ...this.result,
          list: [],
          page: this.result?.page ?? 0,
          pageCount: this.result?.pageCount ?? 0,
          count: this.result?.count ?? 0,
        };
      }

      const { data, status } = await this.service({ ...this.params, signal });
      this.result = data;
      this.status = status;

      if (this.infinite) {
        this.items = this.items.concat(this.result.list);
      } else {
        this.items = this.result.list;
      }

      this.onLoad?.(this.result);
      this.loaded = true;
    } catch (e) {
      if (signal.aborted) {
        console.warn('Request was aborted.');
      } else {
        console.error(e);
        this.error = true;
        this.onError?.(e);
      }
    } finally {
      this.loading = false;
      if (this.result) {
        this.onSuccess?.(this.result);
      }
    }
  }
}
