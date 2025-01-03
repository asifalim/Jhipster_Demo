export interface IMovies {
  id: number;
  name?: string | null;
  category?: string | null;
  rating?: number | null;
}

export type NewMovies = Omit<IMovies, 'id'> & { id: null };
