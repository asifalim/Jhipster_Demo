import { IMovies, NewMovies } from './movies.model';

export const sampleWithRequiredData: IMovies = {
  id: 81167,
};

export const sampleWithPartialData: IMovies = {
  id: 1233,
  category: 'Gorgeous',
};

export const sampleWithFullData: IMovies = {
  id: 22049,
  name: 'Computers Shirt',
  category: 'Georgia',
  rating: 16059,
};

export const sampleWithNewData: NewMovies = {
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
