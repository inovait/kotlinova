git tag -l 'v[0-9].[0-9].[0-9]' --sort=v:refname | cut -c 2- |  while read line ; do
   git tag $line
done