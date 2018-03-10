import treelite
import sys

if len(sys.argv) != 3:
  print('Usage: {} [model file] [model format]'.format(sys.argv[0]));
  print('       model format must be either \'xgboost\' or \'lightgbm\'')
  sys.exit(1)

if sys.argv[2] != 'xgboost' and sys.argv[2] != 'lightgbm':
  print('Unsupported model format')
  print('       model format must be either \'xgboost\' or \'lightgbm\'')
  sys.exit(1)

model = treelite.Model.load(sys.argv[1], sys.argv[2])
model.compile('./model', params={'quantize':1}, verbose=True)
